package com.googlecode.greysanatomy.agent;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;

import com.googlecode.greysanatomy.GreysAnatomyMain;
import com.googlecode.greysanatomy.util.ConfigUtils;

public class AgentMain {

	public static void premain(String args, Instrumentation inst) {
		main(args, inst);
	}
	
	public static void agentmain(String args, Instrumentation inst) {
		main(args, inst);
	}
	
	private static int getPort(String args) {
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
	
	public static synchronized void main(final String args, final Instrumentation inst) {
//		AgentServer.init(inst, ConfigUtils.DEFAULT_AGENT_SERVER_PORT);
		try {
			URLClassLoader agentLoader = new URLClassLoader(new URL[]{new URL("file:"+GreysAnatomyMain.JARFILE)});
			
			final int port = getPort(args);
			agentLoader.loadClass("com.googlecode.greysanatomy.network.AgentServer").getMethod("init",Instrumentation.class, int.class).invoke(null, inst, port);
			
		}catch(Throwable t) {
			t.printStackTrace();
		}
		
	}
	
}
