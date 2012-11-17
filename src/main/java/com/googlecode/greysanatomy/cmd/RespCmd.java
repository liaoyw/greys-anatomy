package com.googlecode.greysanatomy.cmd;

/**
 * ����Ӧ��
 * @author vlinux
 *
 */
public class RespCmd extends BaseCmd {

	private static final long serialVersionUID = -2324080684662002481L;
	
	/**
	 * �������Ϣ������
	 * @author vlinux
	 *
	 */
	public static interface RespCmdSender {

		/**
		 * �����������Ϣ
		 * @param respCmd
		 */
		void send(RespCmd respCmd);
		
	}
	
	private final String message;	//Ӧ����Ϣ����
	private final Throwable cause;

	public RespCmd(long id, String message) {
		super(id);
		this.message = message;
		this.cause = null;
	}
	
	public RespCmd(long id, String message, Throwable cause) {
		super(id);
		this.message = message;
		this.cause = cause;
	}

	/**
	 * ��ȡӦ����Ϣ����
	 * @return ��ϢӦ������
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * ��ȡʧ���쳣��ջ
	 * @return
	 */
	public Throwable getCause() {
		return cause;
	}
	
}
