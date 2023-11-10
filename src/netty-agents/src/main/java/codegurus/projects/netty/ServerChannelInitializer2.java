package codegurus.projects.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

public class ServerChannelInitializer2 extends ChannelInitializer<SocketChannel> {
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private final SslContext sslCtx;
    private ServerChannelHandler serverChannelHandler;

    public ServerChannelInitializer2(SslContext sslCtx, ServerChannelHandler serverChannelHandler) {
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
        pipeline.addLast(new LineBasedFrameDecoder(2048));
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new StringDecoder());

        // and then business logic.
        pipeline.addLast(serverChannelHandler);
    }
}
