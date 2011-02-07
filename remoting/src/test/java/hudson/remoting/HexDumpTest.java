package hudson.remoting;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class HexDumpTest {
    @Test
    public  void test1() {
        assertEquals("0001ff",HexDump.toHex(new byte[]{0,1,-1}));
    }
}
