package codegurus.projects.netty;

import codegurus.projects.netty.codec.MekDecoder;
import codegurus.projects.netty.codec.MekEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private final SslContext sslCtx;
    private ClientChannelHandler clientChannelHandler;

    public ClientChannelInitializer(SslContext sslCtx, ClientChannelHandler clientChannelHandler) {
        this.sslCtx = sslCtx;
        this.clientChannelHandler = clientChannelHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }

//        pipeline.addLast(new DelimiterBasedFrameDecoder(2048, Unpooled.wrappedBuffer(new byte[]{10})));
//        pipeline.addLast(new DelimiterBasedFrameDecoder(2048,Delimiters.lineDelimiter()));
        pipeline.addLast(new MekDecoder());
        pipeline.addLast(new MekEncoder());

        // and then business logic.
        pipeline.addLast(clientChannelHandler);
    }
}
