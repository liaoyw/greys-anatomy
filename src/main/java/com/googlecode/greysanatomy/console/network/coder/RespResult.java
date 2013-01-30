package com.googlecode.greysanatomy.console.network.coder;

import java.io.Serializable;

/**
 * �������Ӧ���
 * @author chengtongda
 *
 */
public class RespResult implements Serializable{
	private static final long serialVersionUID = 661800158888334705L;

	private int jobId;
	
	private long sessionId;
	
	private int pos;
	
	private String message;
	
	private long jobMillis;
	
	private boolean isFinish;

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isFinish() {
		return isFinish;
	}

	public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public long getJobMillis() {
		return jobMillis;
	}

	public void setJobMillis(long createMillis) {
		this.jobMillis = createMillis;
	}
	
}
