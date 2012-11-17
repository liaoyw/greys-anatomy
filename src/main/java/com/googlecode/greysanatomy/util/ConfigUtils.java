package com.googlecode.greysanatomy.util;

/**
 * ≈‰÷√π§æﬂ¿‡
 * @author vlinux
 *
 */
public class ConfigUtils {

	public static final int DEFAULT_AGENT_SERVER_PORT = 7631;
	
	public static final int DEFAULT_CONNECT_TIMEOUT = 60000;
	
	public static int getPort(String args) {
		if( null == args
				|| args.isEmpty()) {
			return ConfigUtils.DEFAULT_AGENT_SERVER_PORT;
		}
		final String[] strs = args.split(",");
		try {
			return Integer.valueOf(strs[0]);
		}catch(Exception e) {
			return ConfigUtils.DEFAULT_AGENT_SERVER_PORT;
		}
	}
	
}
