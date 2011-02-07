package hudson.remoting;

import hudson.remoting.ChannelRunner.Fork;
import hudson.remoting.ChannelRunner.InProcess;
import hudson.remoting.ChannelRunner.InProcessCompatibilityMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

public class ChannelRule extends TestWatchman {
    public enum Type {
        IN_PROCESS {
            @Override
            public ChannelRunner getChannelRunner() {
                return new InProcess();
            }
        },
        IN_PROCESS_COMPATIBILITY_MODE {
            @Override
            public ChannelRunner getChannelRunner() {
                return new InProcessCompatibilityMode();
            }
        },
        FORK {
            @Override
            public ChannelRunner getChannelRunner() {
                return new Fork();
            }
        };
        
        public abstract ChannelRunner getChannelRunner();
    }
    
    public static Collection<Object[]> getParameters() {
        final Type[] values = Type.values();
        final List<Object[]> vals = new ArrayList<Object[]>(values.length);
        for (final Type type : values) {
            vals.add(new Object[] { type });
        }
        return vals;
    }
    
    private final ChannelRunner channelRunner;
    private final Type type;
    private Channel channel;
    
    public ChannelRule(final Type type) {
        this.type = type;
        channelRunner = type.getChannelRunner();
    }
    
    @Override
    public void finished(final FrameworkMethod method) {
        try {
            channelRunner.stop(channel);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void starting(final FrameworkMethod method) {
        try {
            channel = channelRunner.start();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Channel getChannel() {
        return channel;
    }
    
    public Type getType() {
        return type;
    }
}
