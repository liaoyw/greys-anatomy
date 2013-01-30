package com.googlecode.greysanatomy.console.rmi.req;


/**
 * ����ɱ������
 * @author chengtongda
 *
 */
public class ReqKillJob extends GaRequest {
	private static final long serialVersionUID = 7156731632312708537L;
	
	private final String jobId;
	
	public ReqKillJob(long sessionId, String jobId){
		setGaSessionId(sessionId);
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}
	
}
