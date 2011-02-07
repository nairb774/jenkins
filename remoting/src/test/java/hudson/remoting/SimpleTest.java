/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.remoting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.concurrent.CancellationException;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.Bug;

/**
 * Testing the basic features.
 * 
 * @author Kohsuke Kawaguchi
 */
@RunWith(Parameterized.class)
public class SimpleTest {
    @Parameters
    public static Collection<Object[]> getParameters() {
        return ChannelRule.getParameters();
    }
    
    @Rule
    public final ChannelRule channelRule;
    
    public SimpleTest(final ChannelRule.Type type) {
        channelRule = new ChannelRule(type);
    }
    
    @Test
    public void test1() throws Exception {
        int r = channelRule.getChannel().call(new Callable1());
        System.out.println("result=" + r);
        assertEquals(5,r);
    }

    @Test
    public void test1Async() throws Exception {
        Future<Integer> r = channelRule.getChannel().callAsync(new Callable1());
        System.out.println("result="+r.get());
        assertEquals(5,(int)r.get());
    }

    private static class Callable1 implements Callable<Integer, RuntimeException> {
        public Integer call() throws RuntimeException {
            System.err.println("invoked");
            return 5;
        }
    }

    @Test
    public void test2() throws Exception {
        try {
            channelRule.getChannel().call(new Callable2());
            fail();
        } catch (RuntimeException e) {
            assertEquals(e.getMessage(),"foo");
        }
    }

    @Test
    public void test2Async() throws Exception {
        try {
            Future<Integer> r = channelRule.getChannel().callAsync(new Callable2());
            r.get();
            fail();
        } catch (ExecutionException e) {
            assertEquals(e.getCause().getMessage(),"foo");
        }
    }

    private static class Callable2 implements Callable<Integer, RuntimeException> {
        public Integer call() throws RuntimeException {
            throw new RuntimeException("foo");
        }
    }

    /**
     * Makes sure that proxied object can be sent back to the origin and resolve correctly.
     */
    @Test
    public void test3() throws Exception {
        Foo c = new Foo() {};
        Channel channel = channelRule.getChannel();
        Foo r = channel.call(new Echo<Foo>(channel.export(Foo.class,c)));
        assertSame(c,r);
    }

    public static interface Foo {}

    private static class Echo<T> implements Callable<T,RuntimeException> {
        private final T t;

        Echo(T t) {
            this.t = t;
        }

        public T call() throws RuntimeException {
            return t;
        }
    }

    /**
     * Checks whether {@link Future#cancel} behaves according to spec.
     * Currently seems to be used by MavenBuilder.call and Proc.RemoteProc.kill
     * (in turn used by MercurialSCM.joinWithTimeout when polling on remote host).
     */
    @Bug(4611)
    @Test
    public void testCancellation() throws Exception {
        Cancellable task = new Cancellable();
        Future<Integer> r = channelRule.getChannel().callAsync(task);
        r.cancel(true);
        try {
            r.get();
            fail("should not return normally");
        } catch (CancellationException x) {
            // right
        }
        assertTrue(r.isCancelled());
        assertFalse(task.ran);
        // XXX ought to also test various other aspects: cancelling before start, etc.
    }
    private static class Cancellable implements Callable<Integer, InterruptedException> {
        boolean ran;
        public Integer call() throws InterruptedException {
            Thread.sleep(9999);
            ran = true;
            return 0;
        }
    }
}
