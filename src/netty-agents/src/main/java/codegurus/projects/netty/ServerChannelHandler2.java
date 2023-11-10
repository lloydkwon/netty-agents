package codegurus.projects.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ChannelHandler.Sharable
public class ServerChannelHandler2 extends SimpleChannelInboundHandler<String> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerChannelHandler2.class);
    private long count = 0;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("channelActive "+channelId(ctx));
    }
    private String channelId(ChannelHandlerContext ctx) {
        return ctx.channel().id().asShortText();
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("channelInactive "+channelId(ctx));
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String input) throws Exception {

        ctx.channel().writeAndFlush(NettyUtils.appendNewLine(input));
        if (++count % 100000 == 0) {
            LOG.info("channelRead0 "+input);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(),cause);
        ctx.close();
    }

}
