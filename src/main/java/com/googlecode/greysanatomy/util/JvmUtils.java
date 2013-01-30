package com.googlecode.greysanatomy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JVM������ع�����
 * @author vlinux
 *
 */
public class JvmUtils {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	/**
	 * �رչ���
	 * @author vlinux
	 *
	 */
	public static interface ShutdownHook {
		
		/**
		 * ���Թر�
		 * @throws Throwable
		 */
		void shutdown() throws Throwable;
		
	}
	
	/**
	 * ��JVMע��һ���رյ�Hook
	 * @param name
	 * @param shutdownHook
	 */
	public static void registShutdownHook(final String name, final ShutdownHook shutdownHook) {
		
		logger.info("regist shutdown hook {}", name);
		Runtime.getRuntime().addShutdownHook(new Thread(){

			@Override
			public void run() {
				try {
					shutdownHook.shutdown();
					logger.info("{} shutdown successed.", name);
				}catch(Throwable t) {
					logger.warn("{} shutdown failed, ignore it.", name, t);
				}
			}
			
		});
		
	}
	
}
