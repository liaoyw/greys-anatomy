package com.googlecode.greysanatomy;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.network.AgentClient;
import com.googlecode.greysanatomy.util.ConfigUtils;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Hello world!
 * 
 */
public class GreysAnatomyMain {
	
	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	public static final String JARFILE = GreysAnatomyMain.class.getProtectionDomain().getCodeSource().getLocation().getFile();
	
	public static void main(String[] args) throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException {
		
		if( args.length < 1 ) {
			throw new IllegalArgumentException("need input pid.");
		}
		
		VirtualMachineDescriptor attachVmd = null;
		for( VirtualMachineDescriptor vmd : VirtualMachine.list() ) {
			if( vmd.id().equals(args[0]) ) {
				attachVmd = vmd;
				break;
			}
		}//for
		
		if( null == attachVmd ) {
			throw new IllegalArgumentException("pid not existed.");
		}
		
		VirtualMachine vm = VirtualMachine.attach(attachVmd);
		
		if( args.length >= 2 ) {
			vm.loadAgent(JARFILE, args[1]);
		} else {
			vm.loadAgent(JARFILE);
		}
		
		
		vm.detach();
		
		AgentClient.init(ConfigUtils.DEFAULT_AGENT_SERVER_PORT, ConfigUtils.DEFAULT_CONNECT_TIMEOUT);
		logger.info("attach done! pid={}; JarFile={}", args[0], JARFILE);
		
	}
}
