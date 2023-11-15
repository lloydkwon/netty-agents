package codegurus.projects.netty;

import codegurus.projects.mek.Packet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@ChannelHandler.Sharable
public class ClientChannelHandler extends SimpleChannelInboundHandler<Packet> {
    private static final Logger LOG = LoggerFactory.getLogger(ClientChannelHandler.class);
    private Map<String, BlockingQueue<Packet>> map;

    public ClientChannelHandler(Map<String, BlockingQueue<Packet>> map) {
        this.map = map;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String channelId = channelId(ctx);
        LOG.info("channelActive "+channelId);
        map.put(channelId, new LinkedBlockingQueue<>());
//        sendNoOkMsg(ctx);
        sendOkMsg(ctx);

    }

    private void sendNoOkMsg(ChannelHandlerContext ctx) throws Exception {
        String s = FileUtils.readFileToString(new File("./data/13H"), "UTF-8");
        byte[] bytes = Base64.decodeBase64(s);
        Packet packet = new Packet();
        packet.setHeader(bytes[0]);
        packet.setData1(bytes[1]);
        packet.setData2(bytes[2]);
        ctx.channel().writeAndFlush(packet);

    }

    private void sendOkMsg(ChannelHandlerContext ctx) throws Exception {
        String s = FileUtils.readFileToString(new File("./data/10H"), "UTF-8");
        byte[] bytes = Base64.decodeBase64(s);
        Packet packet = new Packet();
        packet.setHeader(bytes[0]);
        packet.setData1(bytes[1]);
        packet.setData2(bytes[2]);
        ctx.channel().writeAndFlush(packet);
//        Thread.sleep(10);
//        ctx.channel().writeAndFlush(packet);
//        Thread.sleep(10);
//        ctx.channel().writeAndFlush(packet);
//        Thread.sleep(10);
//        ctx.channel().writeAndFlush(packet);
    }

    private String channelId(ChannelHandlerContext ctx) {
        return ctx.channel().id().asShortText();
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = channelId(ctx);
        map.remove(channelId);
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
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        if (packet.isOk()) {
            map.get(channelId(ctx)).put(packet);
            ctx.writeAndFlush(packet);
//            LOG.info("OK "+input);
        } else {
//            LOG.info("NO "+input);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(),cause);
        ctx.close();
    }
}
