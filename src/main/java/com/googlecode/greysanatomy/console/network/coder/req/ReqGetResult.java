package com.googlecode.greysanatomy.console.network.coder.req;


/**
 * 请求job执行结果
 * @author chengtongda
 *
 */
public class ReqGetResult extends GaRequest {
	private static final long serialVersionUID = 7156731632312708537L;

	private final long jobId;
	
	private final long jobMillis;
	
	private final int pos;
	
	public ReqGetResult(long jobId, long sessionId, long jobMillis, int pos) {
		this.jobId = jobId;
		this.pos = pos;
		this.jobMillis = jobMillis;
		setGaSessionId(sessionId);
	}
	
	public long getJobId() {
		return jobId;
	}

	public int getPos() {
		return pos;
	}

	public long getJobMillis() {
		return jobMillis;
	}
	
}
