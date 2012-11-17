package com.googlecode.greysanatomy.network;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.cmd.BaseCmd;
import com.googlecode.greysanatomy.cmd.CmdConsole;
import com.googlecode.greysanatomy.cmd.CmdConsole.CmdCallback;
import com.googlecode.greysanatomy.cmd.CmdConsole.CmdConsoleException;
import com.googlecode.greysanatomy.cmd.action.EmptyActionCmd;
import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.network.coder.CmdDecoder;
import com.googlecode.greysanatomy.network.coder.CmdEncoder;
import com.googlecode.greysanatomy.network.coder.ProtocolDecoder;
import com.googlecode.greysanatomy.network.coder.ProtocolEncoder;
import com.googlecode.greysanatomy.util.JvmUtils;
import com.googlecode.greysanatomy.util.JvmUtils.ShutdownHook;

/**
 * 代理客户端
 * @author vlinux
 *
 */
public class AgentClient implements CmdCallback {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	private final ClientBootstrap bootstrap;
	private final ChannelGroup channelGroup;
	private final CmdConsole cmdConsole;
	private final Channel channel;
	
	/*
	 * 业务处理器
	 */
	private SimpleChannelUpstreamHandler businessHandler = new SimpleChannelUpstreamHandler() {

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {

			if( null == e.getMessage()
					|| !(e.getMessage() instanceof RespCmd)) {
				super.messageReceived(ctx, e);
			}
			
			final RespCmd respCmd = (RespCmd)e.getMessage();
			
			cmdConsole.printRespCmd(respCmd);
		}

	};
	
	private ChannelPipelineFactory channelPipelineFactory = new ChannelPipelineFactory() {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("protocol-decoder", new ProtocolDecoder());
			pipeline.addLast("cmd-decoder", new CmdDecoder());
			pipeline.addLast("businessHandler", businessHandler);
			pipeline.addLast("protocol-encoder", new ProtocolEncoder());
			pipeline.addLast("cmd-encoder", new CmdEncoder());
			return pipeline;
		}

	};
	
	private AgentClient(int port, int connectTimeout) {
		
		channelGroup = new DefaultChannelGroup();
		bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool())
					);
				
		bootstrap.setPipelineFactory(channelPipelineFactory);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("connectTimeoutMillis", connectTimeout);
		
		cmdConsole = new CmdConsole(this);
		channel = createChannel(new InetSocketAddress(port));
		
		JvmUtils.registShutdownHook("agent-client", new ShutdownHook(){

			@Override
			public void shutdown() throws Throwable {
				if( null != channelGroup ) {
					channelGroup.close();
				}
				if( null != bootstrap ) {
					bootstrap.releaseExternalResources();
				}
			}
			
		});
		
		Thread heartBeatThread = new Thread() {

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						//
					}
					if( null == channel || !channel.isConnected() ) {
						// 链接已关闭，客户端留着也没啥意思了，在这里退出JVM
						logger.info("disconnect to agent-server, shutdown jvm.");
						Runtime.getRuntime().exit(0);
						break;
					} else {
						// 这里只用发就好了
						channel.write(new EmptyActionCmd());
					}
				}
				
			}
			
		};
		heartBeatThread.setDaemon(true);
		heartBeatThread.start();
		
		
	}
	
	/**
	 * 创建netty的channel
	 * @param address
	 * @return 如果失败则返回null
	 */
	private Channel createChannel(InetSocketAddress address) {
		final ChannelFuture future = bootstrap.connect(address);
		future.awaitUninterruptibly();
		if( future.isCancelled() ) {
			logger.warn("connect to %s cancelled. address:%s", address);
			return null;
		}
		if(!future.isSuccess() ) {
			logger.warn("connect to %s failed. address:%s", address, future.getCause());
			return null;
		}
		
		logger.info("connect to {} successed.", address);
		final Channel channel = future.getChannel();
		channelGroup.add(channel);
		return channel;
	}

	@Override
	public void callback(BaseCmd cmd, CmdConsole console)
			throws CmdConsoleException {
		channel.write(cmd);
	}
	
	private static AgentClient instance;
	
	public static synchronized void init(int port, int connectTimeout) {
		if( null == instance ) {
			instance = new AgentClient(port, connectTimeout);
		}
	}
	
}
