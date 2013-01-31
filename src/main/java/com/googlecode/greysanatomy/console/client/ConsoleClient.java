package com.googlecode.greysanatomy.console.client;

import java.io.IOException;
import java.rmi.Naming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.Configer;
import com.googlecode.greysanatomy.console.GreysAnatomyConsole;
import com.googlecode.greysanatomy.console.rmi.req.ReqHeart;
import com.googlecode.greysanatomy.console.server.ConsoleServerService;
import com.googlecode.greysanatomy.exception.ConsoleException;

/**
 * ����̨�ͻ���
 * @author chengtongda
 *
 */
public class ConsoleClient {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	private final ConsoleServerService consoleServer;
	private final long sessionId;
	
	private ConsoleClient(Configer configer) throws Exception {
		this.consoleServer = (ConsoleServerService)Naming.lookup("rmi://127.0.0.1:"+configer.getConsolePort()+"/RMI_GREYS_ANATOMY");
		this.sessionId = this.consoleServer.register();
		final GreysAnatomyConsole console = new GreysAnatomyConsole(configer, sessionId);
		console.start(consoleServer);
		heartBeat();
	}
	
	/**
	 * ������������߳�
	 */
	private void heartBeat() {
		Thread heartBeatDaemon = new Thread("ga-console-client-heartbeat"){

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						//
					}
					if( null == consoleServer ) {
						// �����ѹرգ��ͻ�������Ҳûɶ��˼�ˣ��������˳�JVM
						logger.info("disconnect to ga-console-server, shutdown jvm.");
						System.exit(0);
						break;
					} else {
						boolean hearBeatResult = false;
						try {
							hearBeatResult = consoleServer.sessionHeartBeat(new ReqHeart(sessionId));
						} catch (Exception e) {
							// 
						}
						//�������ʧ�ܣ���˵����ʱ�ˣ��Ǿ�gg��
						if(!hearBeatResult){
							logger.info("session time out to ga-console-server, shutdown jvm.");
							System.exit(0);
							break;
						}
					}
				}
			}
			
		};
		heartBeatDaemon.setDaemon(true);
		heartBeatDaemon.start();
	}
	
	private static ConsoleClient instance;
	
	/**
	 * ��������̨�ͻ���
	 * @param configer
	 * @throws ConsoleException
	 * @throws IOException 
	 */
	public static synchronized void getInstance(Configer configer) throws Exception {
		if( null == instance ) {
			instance = new ConsoleClient(configer);
		}
	}
	
}
