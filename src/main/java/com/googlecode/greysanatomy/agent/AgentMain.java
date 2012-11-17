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
	
	public static synchronized void main(final String args, final Instrumentation inst) {
//		AgentServer.init(inst, ConfigUtils.DEFAULT_AGENT_SERVER_PORT);
		try {
			URLClassLoader agentLoader = new URLClassLoader(new URL[]{new URL("file:"+GreysAnatomyMain.JARFILE)});
			agentLoader.loadClass("com.googlecode.greysanatomy.network.AgentServer").getMethod("init",Instrumentation.class, int.class).invoke(null, inst, ConfigUtils.DEFAULT_AGENT_SERVER_PORT);
			
		}catch(Throwable t) {
			t.printStackTrace();
		}
		
	}
	
}
