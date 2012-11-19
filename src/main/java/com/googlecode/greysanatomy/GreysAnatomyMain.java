package com.googlecode.greysanatomy;

import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.console.network.ConsoleClient;
import com.googlecode.greysanatomy.exception.ConsoleException;
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
	
	public GreysAnatomyMain(String[] args) throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException, ConsoleException {
		final OptionParser parser = new OptionParser();
		parser.accepts("pid").withRequiredArg().ofType(int.class).required();
		parser.accepts("port").withOptionalArg().ofType(int.class);
		final OptionSet os = parser.parse(args);
		
		final Configer configer = new Configer();
		if( os.has("port") ) {
			configer.setConsolePort((Integer)os.valueOf("port"));
		}
		configer.setJavaPid((Integer)os.valueOf("pid"));
		
		VirtualMachineDescriptor attachVmd = null;
		for( VirtualMachineDescriptor vmd : VirtualMachine.list() ) {
			if( vmd.id().equals(""+configer.getJavaPid()) ) {
				attachVmd = vmd;
				break;
			}
		}//for
		
		if( null == attachVmd ) {
			throw new IllegalArgumentException("pid:"+configer.getJavaPid()+" not existed.");
		}
		
		VirtualMachine vm = null;
		try {
			vm = VirtualMachine.attach(attachVmd);
			vm.loadAgent(JARFILE, configer.toString());
		}finally {
			if( null != vm ) {
				vm.detach();
			}
		}
		
		ConsoleClient.getInstance(configer);
		logger.info("attach done! pid={}; port={}; JarFile={}", new Object[]{
				configer.getJavaPid(), 
				configer.getConsolePort(), 
				JARFILE});
	}
	
	public static void main(String[] args)  {
		
		try {
			new GreysAnatomyMain(args);
		}catch(Throwable t) {
			logger.error("start greys-anatomy failed.",t);
			System.exit(-1);
		}
		
	}
}
