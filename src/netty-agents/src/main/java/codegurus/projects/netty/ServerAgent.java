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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

		Thread.sleep(10000000);
	}
}
