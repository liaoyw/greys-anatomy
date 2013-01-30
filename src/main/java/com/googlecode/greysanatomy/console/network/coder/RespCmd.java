package com.googlecode.greysanatomy.console.network.coder;

/**
 * Ӧ������
 * @author vlinux
 *
 */
public class RespCmd extends CmdTracer {

	private static final long serialVersionUID = -6448961415701231840L;
	
	/*
	 * Ӧ����Ϣ
	 */
	private final String message;
	
	/*
	 * �����Ƿ����
	 */
	private final boolean isFinish;

	/**
	 * ����Ӧ������
	 * @param id
	 * @param isFinish
	 * @param message
	 */
	public RespCmd(long id, boolean isFinish, String message) {
		super(id);
		this.isFinish = isFinish;
		this.message = message;
	}

	/**
	 * ��ȡӦ����Ϣ
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * �����Ƿ����
	 * @return
	 */
	public boolean isFinish() {
		return isFinish;
	}
	
}
