package com.googlecode.greysanatomy.cmd;

/**
 * 命令应答
 * @author vlinux
 *
 */
public class RespCmd extends BaseCmd {

	private static final long serialVersionUID = -2324080684662002481L;
	
	/**
	 * 命令返回信息发送者
	 * @author vlinux
	 *
	 */
	public static interface RespCmdSender {

		/**
		 * 发送命令返回信息
		 * @param respCmd
		 */
		void send(RespCmd respCmd);
		
	}
	
	private final String message;	//应答消息内容
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
	 * 获取应答消息内容
	 * @return 消息应答内容
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 获取失败异常堆栈
	 * @return
	 */
	public Throwable getCause() {
		return cause;
	}
	
}
