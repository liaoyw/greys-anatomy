package com.googlecode.greysanatomy.console.command;

import java.lang.instrument.Instrumentation;

import org.jboss.netty.channel.Channel;

/**
 * ����������
 * @author vlinux
 *
 */
public abstract class Command {

	/**
	 * ��Ϣ������
	 * @author vlinux
	 *
	 */
	public static interface Sender {
		
		/**
		 * ������Ϣ
		 * @param isF
		 * @param message
		 */
		void send(boolean isF, String message);
		
	}
	
	
	/**
	 * ������Ϣ
	 * @author vlinux
	 *
	 */
	public static class Info {
		
		private final Instrumentation inst;
		private final Channel channel;
		
		public Info(Instrumentation inst, Channel channel) {
			this.inst = inst;
			this.channel = channel;
		}

		public Instrumentation getInst() {
			return inst;
		}

		public Channel getChannel() {
			return channel;
		}
		
	}
	
	/**
	 * �����
	 * @author vlinux
	 *
	 */
	public interface Action {

		/**
		 * ִ�ж���
		 * @param info
		 * @param sender
		 * @throws Throwable
		 */
		void action(Info info, Sender sender) throws Throwable;
		
	}
	
	/**
	 * ��ȡ�����
	 * @return
	 */
	abstract public Action getAction() ;
	
	
}
