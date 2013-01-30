package com.googlecode.greysanatomy.console.network.coder.req;

import java.io.Serializable;

/**
 * request
 * @author chengtongda
 *
 */
public class GaRequest implements Serializable {
	private static final long serialVersionUID = 3425339787742173576L;
	
	/**
	 * ∑√Œ ª·ª∞
	 */
	private long gaSessionId;

	public long getGaSessionId() {
		return gaSessionId;
	}

	public void setGaSessionId(long gaSessionId) {
		this.gaSessionId = gaSessionId;
	}
	
}
