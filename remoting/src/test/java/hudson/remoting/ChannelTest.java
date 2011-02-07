package hudson.remoting;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

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
public class ChannelTest {
    @Parameters
    public static Collection<Object[]> getParameters() {
        return ChannelRule.getParameters();
    }
    
    @Rule
    public final ChannelRule channelRule;
    
    public ChannelTest(final ChannelRule.Type type) {
        channelRule = new ChannelRule(type);
    }
    
    @Test
    public void testCapability() {
        assumeThat(channelRule.getType(), not(equalTo(ChannelRule.Type.IN_PROCESS_COMPATIBILITY_MODE)));
        assertTrue(channelRule.getChannel().remoteCapability.supportsMultiClassLoaderRPC());
    }
}
