package com.googlecode.greysanatomy.console.network.coder;

/**
 * ��������
 * @author vlinux
 *
 */
public class ReqCmd extends CmdTracer {

	private static final long serialVersionUID = 7156731632312708537L;

	/*
	 * ��������ԭʼ�ַ���
	 */
	private final String command;
	
	/**
	 * ��������캯��
	 * @param command
	 */
	public ReqCmd(String command) {
		this.command = command;
	}

	/**
	 * ��ȡ��������ԭʼ�ַ���
	 * @return
	 */
	public String getCommand() {
		return command;
	}
	
}
