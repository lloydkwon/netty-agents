package codegurus.projects.netty;

import codegurus.projects.mek.MekDecoder;
import codegurus.projects.mek.MekEncoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;

import javax.net.ssl.SSLEngine;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private final SslContext sslCtx;
    private ServerChannelHandler serverChannelHandler;

    public ServerChannelInitializer(SslContext sslCtx,ServerChannelHandler serverChannelHandler) {
        this.sslCtx = sslCtx;
        this.serverChannelHandler = serverChannelHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }

        // Add the text line codec combination first,
        // the encoder and decoder are static as these are sharable
//        pipeline.addLast(new DelimiterBasedFrameDecoder(2048, Unpooled.wrappedBuffer(new byte[]{10})));
//        pipeline.addLast(new DelimiterBasedFrameDecoder(2048,Delimiters.lineDelimiter()));
        pipeline.addLast(new MekDecoder());
        pipeline.addLast(new MekEncoder());

        // and then business logic.
        pipeline.addLast(serverChannelHandler);
    }
}
