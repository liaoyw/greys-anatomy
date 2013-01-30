package com.googlecode.greysanatomy.console;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;

import jline.console.ConsoleReader;
import jline.console.KeyMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.Configer;
import com.googlecode.greysanatomy.console.command.Commands;
import com.googlecode.greysanatomy.console.network.ConsoleServerService;
import com.googlecode.greysanatomy.console.network.coder.RespResult;
import com.googlecode.greysanatomy.console.network.coder.req.ReqCmd;
import com.googlecode.greysanatomy.console.network.coder.req.ReqGetResult;
import com.googlecode.greysanatomy.console.network.coder.req.ReqKillJob;
import com.googlecode.greysanatomy.util.GaStringUtils;

/**
 * 控制台
 * @author vlinux
 *
 */
public class GreysAnatomyConsole {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	private final Configer configer;
	private final ConsoleReader console;
	
	private volatile boolean isF = true;
	
	private final long sessionId;
	private int jobId;
	private long jobMillis;
	
	/**
	 * 创建GA控制台
	 * @param configer
	 * @throws IOException
	 */
	public GreysAnatomyConsole(Configer configer, long sessionId) throws IOException {
		this.console = new ConsoleReader(System.in, System.out);
		this.configer = configer;
		this.sessionId = sessionId;
		write(GaStringUtils.getLogo());
		Commands.getInstance().registCompleter(console);
	}
	
	/**
	 * 控制台输入者
	 * @author vlinux
	 *
	 */
	private class GaConsoleInputer implements Runnable {

		private final ConsoleServerService consolServer;
		private GaConsoleInputer(ConsoleServerService consolServer) {
			this.consolServer = consolServer;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					doRead();
				}catch(Exception e) {
					// 这里是控制台，可能么？
					logger.warn("console read failed.",e);
				}
			}
		}
		
		private void doRead() throws Exception {
			final String prompt = isF ? configer.getConsolePrompt() : EMPTY;
			final ReqCmd reqCmd = new ReqCmd(console.readLine(prompt), sessionId);
			
			/*
			 * 如果读入的是空白字符串或者当前控制台没被标记为已完成
			 * 则放弃本次所读取内容
			 */
			if( isBlank(reqCmd.getCommand()) || !isF ) {
				return;
			}
			
			// 将命令状态标记为未完成
			isF = false;
			
			// 发送命令请求
			RespResult result =	consolServer.postCmd(reqCmd);
			jobId = result.getJobId();
			jobMillis = result.getJobMillis();
		}
		
	}
	
	/**
	 * 控制台输出者
	 * @author chengtongda
	 *
	 */
	private class GaConsoleOutputer implements Runnable {

		private final ConsoleServerService consolServer;
		private int currentJob = 0;
		private long currentJobMillis = 0;
		private int pos = 0;
		private GaConsoleOutputer(ConsoleServerService consolServer) {
			this.consolServer = consolServer;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					doWrite();
					//每500ms读一次结果
					Thread.sleep(500);
				}catch(Exception e) {
					logger.warn("console write failed.",e);
				}
			}
		}
		
		private void doWrite() throws Exception {
			//如果任务结束，或还没有注册好job  则不读
			if(isF || sessionId == 0 || jobId ==0){
				return;
			}
			
			//如果当前获取结果的job不是正在执行的job，则从0开始读
			if((currentJob != jobId) || (currentJobMillis != jobMillis)){
				pos = 0;
				currentJob = jobId;
				currentJobMillis = jobMillis;
			}
			
			RespResult resp = consolServer.getCmdExecuteResult(new ReqGetResult(jobId, sessionId, jobMillis, pos));
			pos = resp.getPos();
			write(resp);
		}
		
	}
	
	/**
	 * 启动console
	 * @param channel
	 */
	public synchronized void start(final ConsoleServerService consoleServer) {
		this.console.getKeys().bind(""+KeyMap.CTRL_D, new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if( !isF ) {
					write("abort it.");
					isF = true;
					try {
						consoleServer.killJob(new ReqKillJob(sessionId, jobId));
					} catch (Exception e1) {
						// 这里是控制台，可能么？
						logger.warn("killJob failed.",e);
					}
				}
			}
			
		});
		new Thread(new GaConsoleInputer(consoleServer)).start();
		new Thread(new GaConsoleOutputer(consoleServer)).start();
	}
	
	
	/**
	 * 向控制台输出返回信息
	 * @param resp
	 */
	public void write(RespResult resp) {
		if( !isF) {
			if( resp.isFinish() ) {
				isF = true;
			}
			write(resp.getMessage());
		}
	}
	
	/**
	 * 输出信息
	 * @param message
	 */
	private void write(String message) {
		final Writer writer = console.getOutput();
		try {
			writer.write(message+"\n");
			writer.flush();
		}catch(IOException e) {
			// 控制台写失败，可能么？
			logger.warn("console write failed.", e);
		}
		
	}
	
}
