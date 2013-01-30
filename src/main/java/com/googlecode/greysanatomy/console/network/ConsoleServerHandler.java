package com.googlecode.greysanatomy.console.network;

import static com.googlecode.greysanatomy.console.network.ChannelJobsHolder.getJobs;
import static com.googlecode.greysanatomy.console.network.ChannelJobsHolder.unRegistJob;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.console.command.Command;
import com.googlecode.greysanatomy.console.command.Command.Action;
import com.googlecode.greysanatomy.console.command.Command.Info;
import com.googlecode.greysanatomy.console.command.Command.Sender;
import com.googlecode.greysanatomy.console.command.Commands;
import com.googlecode.greysanatomy.console.network.coder.KillJobsCmd;
import com.googlecode.greysanatomy.console.network.coder.ReqCmd;
import com.googlecode.greysanatomy.console.network.coder.RespCmd;
import com.googlecode.greysanatomy.util.JvmUtils;
import com.googlecode.greysanatomy.util.JvmUtils.ShutdownHook;

/**
 * ����̨����˴�����
 * @author vlinux
 *
 */
public class ConsoleServerHandler extends SimpleChannelUpstreamHandler {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	private final Instrumentation inst;
	private final ChannelGroup channelGroup;
	private final ExecutorService workers;
	
	public ConsoleServerHandler(Instrumentation inst, ChannelGroup channelGroup) {
		this.inst = inst;
		this.channelGroup = channelGroup;
		this.workers = Executors.newCachedThreadPool(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "ga-console-server-workers");
				t.setDaemon(true);
				return t;
			}

		});
		
		JvmUtils.registShutdownHook("ga-console-server", new ShutdownHook(){

			@Override
			public void shutdown() throws Throwable {
				if( null != workers ) {
					workers.shutdown();
				}
			}
			
		});
		
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		channelGroup.add(ctx.getChannel());
		logger.info("client:{} was connected.", ctx.getChannel().getRemoteAddress());
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		// ����ע����ǰ�����ϵ���������
		unRegistJob(ctx.getChannel());
		logger.info("client:{} was disconnected.", ctx.getChannel().getRemoteAddress());
	}



	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		if( null == e.getMessage()
				|| !(e.getMessage() instanceof ReqCmd)) {
			super.messageReceived(ctx, e);
		}
		
		final Channel channel = ctx.getChannel();
		final ReqCmd req = (ReqCmd)e.getMessage();
		
		workers.execute(new Runnable(){

			@Override
			public void run() {
				
				// ����ɱ��
				if( req instanceof KillJobsCmd ) {
					unRegistJob(channel, getJobs(channel));
					return;
				}
				
				// ��ͨ��������
				final Info info = new Info(inst, channel);
				final Sender sender = new Sender(){

					@Override
					public void send(boolean isF, String message) {
						write(channel, req, isF, message);
					}
					
				};
				
				try {
					final Command command = Commands.getInstance().newCommand(req.getCommand());
					// �������
					if( null == command ) {
						write(channel, req, true, "command not found.");
						return;
					}
					final Action action = command.getAction();
					action.action(info, sender);
				}catch(Throwable t) {
					// ִ������ʧ��
					logger.warn("do action failed.", t);
					write(channel, req, true, "do action failed. cause:"+t.getMessage());
					return;
				}
			}
			
		});
		
		
		
	}
	
	/**
	 * д��Ϣ
	 * @param channel
	 * @param isF
	 * @param req
	 * @param message
	 */
	private void write(Channel channel, ReqCmd req, boolean isF, String message) {
		channel.write(new RespCmd(req.getId(), isF, message));
	}
	
}
