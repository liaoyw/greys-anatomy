package com.googlecode.greysanatomy.console.network.coder.req;


/**
 * ÇëÇóĞÄÌø
 * @author chengtongda
 *
 */
public class ReqHeart extends GaRequest {
	private static final long serialVersionUID = 7156731632312708537L;
	
	public ReqHeart(long sessionId){
		setGaSessionId(sessionId);
	}
	
}
