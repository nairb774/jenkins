/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc.
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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.SocketException;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Kohsuke Kawaguchi
 */
@RunWith(Parameterized.class)
public class NonSerializableExceptionTest {
    @Parameters
    public static Collection<Object[]> getParameters() {
        return ChannelRule.getParameters();
    }
    
    @Rule
    public final ChannelRule channelRule;
    
    public NonSerializableExceptionTest(final ChannelRule.Type type) {
        channelRule = new ChannelRule(type);
    }
    
    /**
     * Makes sure non-serializable exceptions are gracefully handled.
     *
     * HUDSON-1041.
     */
    @Test
    public void test1() throws Throwable {
        try {
            channelRule.getChannel().call(new Failure());
        } catch (ProxyException p) {
            // verify that we got the right kind of exception
            assertTrue(p.getMessage().contains("NonSerializableException"));
            assertTrue(p.getMessage().contains("message1"));
            ProxyException nested = p.getCause();
            assertTrue(nested.getMessage().contains("SocketException"));
            assertTrue(nested.getMessage().contains("message2"));
            assertNull(nested.getCause());
        }
    }

    private static final class NonSerializableException extends Exception {
        private final Object o = new Object(); // this is not serializable

        private NonSerializableException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    private static final class Failure implements Callable {
        public Object call() throws Throwable {
            throw new NonSerializableException("message1",new SocketException("message2"));
        }
    }
}
