// ---
// Copyright 2020 netty-agents team
// All rights reserved
// ---
package codegurus.projects.netty;

import codegurus.projects.mek.Packet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class ServerAgent extends BasicAgent {
	private Logger logger = LoggerFactory.getLogger(ClientAgent.class);
	private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private ServerBootstrap bootstrap = new ServerBootstrap();

	private AtomicBoolean shutdownRequested = new AtomicBoolean(false);

	private ServerChannelHandler serverChannelHandler;

	public ServerAgent(ServerChannelHandler serverChannelHandler) {
		this.serverChannelHandler = serverChannelHandler;
	}


	@Override
	public void startup(String host,int port) throws Exception {
		final SslContext sslCtx = ServerUtil.buildSslContext();

		this.shutdownRequested.set(false);

		this.bootstrap.group(this.bossGroup, this.workerGroup);
		this.bootstrap.channel(NioServerSocketChannel.class);
		this.bootstrap.childHandler(new ServerChannelInitializer(sslCtx,serverChannelHandler));
		this.bootstrap.option(ChannelOption.SO_BACKLOG, 128);
		this.bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		this.bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		 this.bootstrap.localAddress(new InetSocketAddress(host, port));
		new Thread(() -> maintainConnection(port)).start();
	}

	public void shutdown() {

		try {
			this.shutdownRequested.set(true);

			this.bossGroup.shutdownGracefully();
			this.workerGroup.shutdownGracefully();

		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
		}
	}


	private void maintainConnection(int port) {
		try {
			while (!this.shutdownRequested.get()) {
				ChannelFuture channelFuture = this.bootstrap.bind(port).sync();
				channelFuture.channel().closeFuture().sync();
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public static void main(String[] args) throws Exception {
		BlockingQueue<Packet> blockingQueue = new LinkedBlockingQueue<>();
		ServerAgent serverAgent = new ServerAgent(new ServerChannelHandler(blockingQueue));
		serverAgent.startup("localhost",8099);
		CopyOnWriteArrayList<Packet> list = new CopyOnWriteArrayList<>();
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
		executorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println(".... ");
					Packet packet = blockingQueue.take();
					list.add(packet);
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
			}
		}, 0,1, TimeUnit.NANOSECONDS);
		System.out.println("ok 1");

		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					if (list.size() > 0) {
//						int last = list.size();
//						if (last > 120) {
//							last = 120;
//						}
						int last = 1;
						System.out.println("1list "+list.size());
						List<Packet> packets = new ArrayList<>(list.subList(0, last));
						list.subList(0, last).clear();

						System.out.println("2list "+list.size());
						System.out.println("packets "+packets.size());

					} else {
						//SKIP
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}


			}
		}, 0, 1, TimeUnit.SECONDS);

		System.out.println("ok 2");
		Thread.sleep(10000000);
	}
}
