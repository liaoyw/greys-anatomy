package com.googlecode.greysanatomy.network;

import static com.googlecode.greysanatomy.agent.GreysAnatomyClassFileTransformer.transform;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;

import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.agent.GreysAnatomyClassFileTransformer.TransformResult;
import com.googlecode.greysanatomy.cmd.BaseCmd;
import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.action.ActionCmd;
import com.googlecode.greysanatomy.cmd.probe.ProbeCmd;
import com.googlecode.greysanatomy.network.coder.CmdDecoder;
import com.googlecode.greysanatomy.network.coder.CmdEncoder;
import com.googlecode.greysanatomy.network.coder.ProtocolDecoder;
import com.googlecode.greysanatomy.network.coder.ProtocolEncoder;
import com.googlecode.greysanatomy.probe.ProbeListener;
import com.googlecode.greysanatomy.probe.Probes;
import com.googlecode.greysanatomy.util.JvmUtils;
import com.googlecode.greysanatomy.util.JvmUtils.ShutdownHook;



public class AgentServer {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	private final Instrumentation instrumentation;
	private ServerBootstrap bootstrap;
	private ChannelGroup channelGroup;
	private ExecutorService workers;
	private Map<Channel, List<Integer>> jobsOnChannels = new HashMap<Channel, List<Integer>>();
	
	private ChannelPipelineFactory channelPipelineFactory = new ChannelPipelineFactory() {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("protocol-decoder", new ProtocolDecoder());
			pipeline.addLast("cmd-decoder", new CmdDecoder());
			pipeline.addLast("business-handler", businessHandler);
			pipeline.addLast("protocol-encoder", new ProtocolEncoder());
			pipeline.addLast("cmd-encoder", new CmdEncoder());
			return pipeline;
		}

	};
	
	/*
	 * 业务处理器
	 */
	private SimpleChannelUpstreamHandler businessHandler = new SimpleChannelUpstreamHandler() {
		
		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			super.channelConnected(ctx, e);
			channelGroup.add(ctx.getChannel());
			jobsOnChannels.put(ctx.getChannel(), new ArrayList<Integer>());
			logger.info("client:{} was connected.", ctx.getChannel().getRemoteAddress());
		}

		@Override
		public void channelDisconnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			super.channelDisconnected(ctx, e);
			final List<Integer> jobIds = jobsOnChannels.get(ctx.getChannel());
			if( null != jobIds ) {
				for( Integer jobId : jobIds ) {
					try {
						Probes.killJob(jobId);
					}catch(Throwable t) {
						//
					}
				}
			}
			logger.info("client:{} was disconnected.", ctx.getChannel().getRemoteAddress());
		}



		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {

			if( null == e.getMessage()
					|| !(e.getMessage() instanceof BaseCmd)) {
				super.messageReceived(ctx, e);
			}
			
			final Channel channel = ctx.getChannel();
			final BaseCmd cmd = (BaseCmd)e.getMessage();
			cmd.setInst(instrumentation);
			final RespCmdSender sender = new RespCmdSender(){

				@Override
				public void send(RespCmd respCmd) {
					channel.write(respCmd);
				}
				
			};
			workers.execute(new Runnable(){

				@Override
				public void run() {
					
					try {
						if( cmd instanceof ProbeCmd ) {
							handleProbeCmd((ProbeCmd)cmd);
						} else if( cmd instanceof ActionCmd ) {
							handleActionCmd((ActionCmd)cmd);
						} else {
							//
						}
					}catch(Throwable t) {
						channel.write(new RespCmd(cmd.getId(), "execute failed.\n", t));
						logger.warn("execute failed.", t);
					}
					
				}
				
				/**
				 * 执行监听命令
				 * @param probeCmd
				 * @throws Throwable
				 */
				private void handleProbeCmd(ProbeCmd probeCmd) throws Throwable {
					final ProbeListener listener = probeCmd.getProbeListener(sender);
					
					if( null == listener ) {
						return;
					}
					
					try {
						listener.create();
						TransformResult result = transform(
								instrumentation, probeCmd.getPerfClzRegex(), probeCmd.getPerfMthRegex(), listener);
						jobsOnChannels.get(channel).add(result.getId());
						StringBuilder resultSB = new StringBuilder();
						if( !result.getModifiedClasses().isEmpty() ) {
							for( Class<?> modifiedClass : result.getModifiedClasses() ) {
								resultSB.append(modifiedClass.getName()).append("\n");
							}
							resultSB.append("---------------------------------------------------------------\n");
						}
						resultSB.append(format("done. jobid=%s; total-class=%s; total-method=%s\n", 
								result.getId(), 
								result.getModifiedClasses().size(),
								result.getModifiedMethods().size()));
						channel.write(new RespCmd(probeCmd.getId(), resultSB.toString()));
					}catch(Throwable t){
						try {
							listener.destroy();
						}catch(Throwable dt) {
							logger.warn("destroy listener failed. reqId={}", probeCmd.getId(), dt);
						}
						throw t;
					}
				}
				
				/**
				 * 执行动作命令
				 * @param actionCmd
				 * @throws Throwable
				 */
				private void handleActionCmd(ActionCmd actionCmd) throws Throwable {
					actionCmd.doAction(sender);
				}
				
			});
			
		}
		
	};
	
	private AgentServer(Instrumentation instrumentation, int port) {
		
		this.instrumentation = instrumentation;
		
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				newCachedThreadPool(),
				newCachedThreadPool()));
		bootstrap.setPipelineFactory(channelPipelineFactory);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		channelGroup = new DefaultChannelGroup();
		channelGroup.add(bootstrap.bind(new InetSocketAddress(port)));
		
		workers = Executors.newCachedThreadPool(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "agent-server-workers");
				t.setDaemon(true);
				return t;
			}

		});
		
		logger.info("server was started at port={}", port);
		JvmUtils.registShutdownHook("agent-server", new ShutdownHook(){

			@Override
			public void shutdown() throws Throwable {
				if( null != channelGroup ) {
					channelGroup.close().awaitUninterruptibly();
				}
				if( null != bootstrap ) {
					bootstrap.releaseExternalResources();
				}
				if( null != workers ) {
					workers.shutdown();
				}
			}
			
		});
		
	}
	
	private static AgentServer instance;
	
	/**
	 * 初始化AgentServer
	 * @param instrumentation
	 * @param port
	 */
	public static synchronized void init(Instrumentation instrumentation, int port) {
		if( null == instance ) {
			instance = new AgentServer(instrumentation, port);
		}
	}
	
}
