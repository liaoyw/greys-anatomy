package com.googlecode.greysanatomy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.console.network.ConsoleClient;

/**
 * Hello world!
 * 
 */
public class GreysAnatomyMain {
	
	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	public static final String JARFILE = GreysAnatomyMain.class.getProtectionDomain().getCodeSource().getLocation().getFile();
	
	public GreysAnatomyMain(String[] args) throws Exception {
		
		// ���������ļ�
		Configer configer = parsetConfiger(args);
		
		// ����agent
		attachAgent(configer);
		
		// �������̨
		activeConsoleClient(configer);
		
		logger.info("attach done! pid={}; port={}; JarFile={}", new Object[]{
				configer.getJavaPid(), 
				configer.getConsolePort(), 
				JARFILE});
	}
	
	/**
	 * ����configer
	 * @param args
	 * @return
	 */
	private Configer parsetConfiger(String[] args) {
		final OptionParser parser = new OptionParser();
		parser.accepts("pid").withRequiredArg().ofType(int.class).required();
		parser.accepts("port").withOptionalArg().ofType(int.class);
		
		final OptionSet os = parser.parse(args);
		
		final Configer configer = new Configer();
		if( os.has("port") ) {
			configer.setConsolePort((Integer)os.valueOf("port"));
		}
		configer.setJavaPid((Integer)os.valueOf("pid"));
		return configer;
	}
	
	/**
	 * ����Agent
	 * @param configer
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	private void attachAgent(Configer configer) throws IOException, ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		final Class<?> vmdClass = loader.loadClass("com.sun.tools.attach.VirtualMachineDescriptor");
		final Class<?> vmClass = loader.loadClass("com.sun.tools.attach.VirtualMachine");
		
		Object attachVmdObj = null;
		for( Object obj : (List<?>)vmClass.getMethod("list", (Class<?>[])null).invoke(null, (Object[])null) ) {
			if( ((String)vmdClass.getMethod("id", (Class<?>[])null).invoke(obj, (Object[])null)).equals(""+configer.getJavaPid()) ) {
				attachVmdObj =  obj;
			}
		}
		
		if( null == attachVmdObj ) {
			throw new IllegalArgumentException("pid:"+configer.getJavaPid()+" not existed.");
		}
		
		Object vmObj = null;
		try {
			vmObj = vmClass.getMethod("attach", vmdClass).invoke(null, attachVmdObj);
			vmClass.getMethod("loadAgent", String.class, String.class).invoke(vmObj, JARFILE, configer.toString());
		} finally {
			if( null != vmObj ) {
				vmClass.getMethod("detach", (Class<?>[])null).invoke(vmObj, (Object[])null);
			}
		}
		
	}
	
	/**
	 * �������̨�ͻ���
	 * @param configer
	 * @throws Exception 
	 */
	private void activeConsoleClient(Configer configer) throws Exception {
		ConsoleClient.getInstance(configer);
	}
	
	
	
	public static void main(String[] args)  {
		
		try {
			new GreysAnatomyMain(args);
		}catch(Throwable t) {
			logger.error("start greys-anatomy failed. because "+t.getMessage(), t);
			System.exit(-1);
		}
		
	}
}
