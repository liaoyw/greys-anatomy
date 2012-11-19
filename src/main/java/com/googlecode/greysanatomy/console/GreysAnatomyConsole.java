package com.googlecode.greysanatomy.console;

import static org.apache.commons.lang.StringUtils.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;

import jline.console.ConsoleReader;
import jline.console.KeyMap;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.Configer;
import com.googlecode.greysanatomy.console.command.Commands;
import com.googlecode.greysanatomy.console.network.coder.ReqCmd;
import com.googlecode.greysanatomy.console.network.coder.RespCmd;
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
	
	/**
	 * ����GA����̨
	 * @param configer
	 * @throws IOException
	 */
	public GreysAnatomyConsole(Configer configer) throws IOException {
		this.console = new ConsoleReader(System.in, System.out);
		this.configer = configer;
		write(GaStringUtils.getLogo());
		Commands.getInstance().registCompleter(console);
	}
	
	/**
	 * ����̨������
	 * @author vlinux
	 *
	 */
	private class GaConsoleInputer implements Runnable {

		private final Channel channel;
		private GaConsoleInputer(Channel channel) {
			this.channel = channel;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					doRead();
				}catch(IOException e) {
					// �����ǿ���̨������ô��
					logger.warn("console read failed.",e);
				}
			}
		}
		
		private void doRead() throws IOException {
			final String prompt = isF ? configer.getConsolePrompt() : EMPTY;
			final ReqCmd reqCmd = new ReqCmd(console.readLine(prompt));
			
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
			channel.write(reqCmd).awaitUninterruptibly();
			
		}
		
	}
	
	/**
	 * ����console
	 * @param channel
	 */
	public synchronized void start(final Channel channel) {
		this.console.getKeys().bind(""+KeyMap.CTRL_D, new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if( !isF ) {
					write("abort it.");
					isF = true;
				}
			}
			
		});
		new Thread(new GaConsoleInputer(channel)).start();
	}
	
	
	/**
	 * �����̨���������Ϣ
	 * @param resp
	 */
	public void write(RespCmd resp) {
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
