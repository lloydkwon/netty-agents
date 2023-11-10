package codegurus.projects.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class ClientAgent extends BasicAgent {

	private Logger logger = LoggerFactory.getLogger(ClientAgent.class);

	private EventLoopGroup workerGroup;
	private Bootstrap bootstrap = new Bootstrap();
	private Channel channel;

	private AtomicBoolean shutdownRequested = new AtomicBoolean(false);

	private ClientChannelHandler clientChannelHandler;

	public ClientAgent(ClientChannelHandler clientChannelHandler,EventLoopGroup workerGroup) {
		this.clientChannelHandler = clientChannelHandler;
		this.workerGroup = workerGroup;
	}

	@Override
	public void startup(String host,int port) throws Exception {
		final SslContext sslCtx = ServerUtil.buildSslContext();
		this.shutdownRequested.set(false);
		this.bootstrap.group(workerGroup);
		this.bootstrap.channel(NioSocketChannel.class);
//		this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		this.bootstrap.option(ChannelOption.TCP_NODELAY, true);
		this.bootstrap.handler(new ClientChannelInitializer(sslCtx,clientChannelHandler));
		this.bootstrap.remoteAddress(new InetSocketAddress(host, port));

		new Thread(() -> maintainConnection()).start();
	}
	@Override
	public void shutdown() {
		try {
			this.shutdownRequested.set(true);;
			this.channel.eventLoop().shutdownGracefully();

		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
		}
	}

	private void maintainConnection() {
		while (!this.shutdownRequested.get()) {
			try {
				ChannelFuture channelFuture = null;
				while (!this.shutdownRequested.get() && (channelFuture = connectSafe()) == null) {
					TimeUnit.SECONDS.sleep(1);
				}

				if (channelFuture != null) {
					this.channel = channelFuture.channel();
					this.channel.closeFuture().sync();
				}
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
	}

	private ChannelFuture connectSafe() {
		try {
			if (!this.shutdownRequested.get()) {
				return this.bootstrap.connect().sync();
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		EventLoopGroup workGroup = new NioEventLoopGroup();
		ClientAgent clientAgent = new ClientAgent(new ClientChannelHandler(),workGroup);
		clientAgent.startup("localhost",8099);
		Thread.sleep(2000);
		clientAgent.shutdown();
	}
}
