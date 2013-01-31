package com.googlecode.greysanatomy.console.command;

import java.lang.instrument.Instrumentation;

import com.googlecode.greysanatomy.console.command.annotation.Arg;

/**
 * ����������
 * @author vlinux
 *
 */
public abstract class Command {

	/**
	 * �ض���·��
	 */
	@Arg(name="o", isRequired=false)
	String redirectPath;
	
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
	 * @author chengtongda
	 *
	 */
	public static class Info {
		
		private final Instrumentation inst;
		private final long sessionId;
		private final String jobId;
		
		public Info(Instrumentation inst, long sessionId, String jobId) {
			this.inst = inst;
			this.sessionId = sessionId;
			this.jobId = jobId;
		}

		public Instrumentation getInst() {
			return inst;
		}

		public long getSessionId() {
			return sessionId;
		}

		public String getJobId() {
			return jobId;
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

	public String getRedirectPath() {
		return redirectPath;
	}

	public void setRedirectPath(String redirectPath) {
		this.redirectPath = redirectPath;
	}
	
}
