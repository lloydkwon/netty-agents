package codegurus.projects.netty;

import codegurus.projects.mek.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.NettyRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
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
//		Map<String, BlockingQueue<Packet>> map = new ConcurrentHashMap<>();
//		EventLoopGroup workGroup = new NioEventLoopGroup();
//		ClientAgent clientAgent = new ClientAgent(new ClientChannelHandler(map),workGroup);
//		clientAgent.startup("localhost",8099);
//		ClientAgent clientAgent2 = new ClientAgent(new ClientChannelHandler(map),workGroup);
//		clientAgent2.startup("localhost",8099);
//		ClientAgent clientAgent3 = new ClientAgent(new ClientChannelHandler(map),workGroup);
//		clientAgent3.startup("localhost",8099);
//		ClientAgent clientAgent4 = new ClientAgent(new ClientChannelHandler(map),workGroup);
//		clientAgent4.startup("localhost",8099);
//		ClientAgent clientAgent5 = new ClientAgent(new ClientChannelHandler(map),workGroup);
//		clientAgent5.startup("localhost",8099);
//		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(NettyRuntime.availableProcessors(), NettyRuntime.availableProcessors()*2, 10,TimeUnit.SECONDS, new LinkedBlockingQueue<>());
//
//		executorService.scheduleAtFixedRate(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					for (String channelId : map.keySet()) {
//						BlockingQueue<Packet> dataQueue = map.get(channelId);
//						if (!dataQueue.isEmpty()) {
//							System.out.println(channelId+ " 1list "+dataQueue.size());
//							List<Packet> batch = new ArrayList<>();
//							dataQueue.drainTo(batch, Math.min(dataQueue.size(), 120));
//							threadPool.submit(new PacketsHandler(batch));
//						}
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//					throw e;
//				}
//
//
//			}
//		}, 0, 200, TimeUnit.MILLISECONDS);
//
//		System.out.println("ok 2");
//		Thread.sleep(1000000);
//		clientAgent.shutdown();
//		clientAgent2.shutdown();
//		clientAgent3.shutdown();
//		clientAgent4.shutdown();
//		clientAgent5.shutdown();
//		workGroup.shutdownGracefully();
//		Thread.sleep(10000);
	}
}
