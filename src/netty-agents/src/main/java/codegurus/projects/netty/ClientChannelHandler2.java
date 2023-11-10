package codegurus.projects.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@ChannelHandler.Sharable
public class ClientChannelHandler2 extends SimpleChannelInboundHandler<String> {
    private static final Logger LOG = LoggerFactory.getLogger(ClientChannelHandler2.class);
    private Map<String, Integer> temp = new HashMap<>();
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("channelActive "+channelId(ctx));
        temp.put(channelId(ctx), new Integer(0));
        ctx.channel().writeAndFlush(NettyUtils.appendNewLine("hiHello"));
    }

    private String channelId(ChannelHandlerContext ctx) {
        return ctx.channel().id().asShortText();
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = channelId(ctx);
        LOG.info("channelInactive "+channelId + " "+temp);
        temp.remove(channelId);
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object _msg) throws Exception {
//        String msg = (String) _msg;
//        System.out.println("channelRead "+msg);
//        Integer integer = temp.get(channelId(ctx));
//        integer = integer+1;
//        temp.put(channelId(ctx), integer);
//        ctx.writeAndFlush(NettyUtils.appendNewLine(msg));
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String channelId = channelId(ctx);
//        LOG.info("channelRead0 "+msg);
//        System.out.println("channelRead0 "+channelId+" "+msg+" "+temp);
        Integer integer = temp.get(channelId);
        integer = integer+1;
        temp.put(channelId, integer);

        if (integer >= 100000) {

            ctx.close();
        }

        int index = msg.indexOf("_");

        String _msg = null;
        if (index == -1) {
            _msg = msg;
        } else {
            _msg = msg.substring(0, index);
        }


        ctx.writeAndFlush(NettyUtils.appendNewLine(_msg+"_"+integer));
//        ctx.writeAndFlush(NettyUtils.appendNewLine(msg+"_"+integer));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(),cause);
        ctx.close();
    }
}
