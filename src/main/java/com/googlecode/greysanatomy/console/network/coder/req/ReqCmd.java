package com.googlecode.greysanatomy.console.network.coder.req;


/**
 * ��������
 * @author chengtongda
 *
 */
public class ReqCmd extends GaRequest {
	private static final long serialVersionUID = 7156731632312708537L;

	/**
	 * ��������ԭʼ�ַ���
	 */
	private String command;
	
	public ReqCmd(String commond, long sessionId) {
		this.command = commond;
		setGaSessionId(sessionId);
	}
	
	/**
	 * ��ȡ��������ԭʼ�ַ���
	 * @return
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * ������������ԭʼ�ַ���
	 * @param command
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
}
