package codegurus.projects.netty;

import codegurus.projects.mek.Packet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.Delimiters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.BlockingQueue;


@ChannelHandler.Sharable
public class ServerChannelHandler extends SimpleChannelInboundHandler<Packet> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerChannelHandler.class);
    private BlockingQueue blockingQueue;

    public ServerChannelHandler(BlockingQueue blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

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
    protected void channelRead0(ChannelHandlerContext ctx, Packet input) throws Exception {

        if (input.isOk() == false) {
//            LOG.info("NO "+input.view());
        } else {
//            LOG.info("OK "+input.view());
//            blockingQueue.put(input);
            ctx.channel().writeAndFlush(input);
            Thread.sleep(10);
//            wait(1000);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(),cause);
        ctx.close();
    }

}
