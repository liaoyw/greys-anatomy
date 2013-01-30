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
 * ����̨
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
	 * ����GA����̨
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
	 * ����̨������
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
					// �����ǿ���̨������ô��
					logger.warn("console read failed.",e);
				}
			}
		}
		
		private void doRead() throws Exception {
			final String prompt = isF ? configer.getConsolePrompt() : EMPTY;
			final ReqCmd reqCmd = new ReqCmd(console.readLine(prompt), sessionId);
			
			/*
			 * ���������ǿհ��ַ������ߵ�ǰ����̨û�����Ϊ�����
			 * �������������ȡ����
			 */
			if( isBlank(reqCmd.getCommand()) || !isF ) {
				return;
			}
			
			// ������״̬���Ϊδ���
			isF = false;
			
			// ������������
			RespResult result =	consolServer.postCmd(reqCmd);
			jobId = result.getJobId();
			jobMillis = result.getJobMillis();
		}
		
	}
	
	/**
	 * ����̨�����
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
					//ÿ500ms��һ�ν��
					Thread.sleep(500);
				}catch(Exception e) {
					logger.warn("console write failed.",e);
				}
			}
		}
		
		private void doWrite() throws Exception {
			//��������������û��ע���job  �򲻶�
			if(isF || sessionId == 0 || jobId ==0){
				return;
			}
			
			//�����ǰ��ȡ�����job��������ִ�е�job�����0��ʼ��
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
	 * ����console
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
						// �����ǿ���̨������ô��
						logger.warn("killJob failed.",e);
					}
				}
			}
			
		});
		new Thread(new GaConsoleInputer(consoleServer)).start();
		new Thread(new GaConsoleOutputer(consoleServer)).start();
	}
	
	
	/**
	 * �����̨���������Ϣ
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
	 * �����Ϣ
	 * @param message
	 */
	private void write(String message) {
		final Writer writer = console.getOutput();
		try {
			writer.write(message+"\n");
			writer.flush();
		}catch(IOException e) {
			// ����̨дʧ�ܣ�����ô��
			logger.warn("console write failed.", e);
		}
		
	}
	
}
