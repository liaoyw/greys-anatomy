package com.googlecode.greysanatomy.cmd;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.util.ClassUtils;

/**
 * �������̨
 * @author vlinux
 *
 */
public class CmdConsole {

//	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	/**
	 * ������ƻص��ӿ�
	 * @author vlinux
	 *
	 */
	public static interface CmdCallback {
		
		/**
		 * ����̨����ص�
		 * @param cmd
		 * @param console
		 * @throws CmdConsoleException
		 */
		void callback(BaseCmd cmd, CmdConsole console) throws CmdConsoleException;
		
	}
	
	/**
	 * �����д����쳣
	 * @author vlinux
	 *
	 */
	public static class CmdConsoleException extends Exception {
		private static final long serialVersionUID = 8161404325484759723L;
		public CmdConsoleException(String arg0) {
			super(arg0);
		}
		public CmdConsoleException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	
	
	private final static Map<String,Class<?>> cmdPools = new HashMap<String,Class<?>>();
	private final PrintWriter pw;
	
	/**
	 * ���������п���̨
	 * @param callback
	 */
	public CmdConsole(final CmdCallback callback) {
		final Scanner scanner = new Scanner(System.in);
		this.pw = new PrintWriter(System.out);
		
		initCmds();
		
		new Thread("ga-cmd-console"){
			
			@Override
			public void run() {
				while(scanner.hasNextLine()) {
					final String line = scanner.nextLine().trim();
					if( line.isEmpty() ) {
						continue;
					}
					try {
						final BaseCmd cmd = parseCmd(line);
						callback.callback(cmd, CmdConsole.this);
					} catch (CmdConsoleException e) {
//						logger.warn("parse args failed, because {}", e.getMessage(), e);
						CmdConsole.this.pw.println("error, "+e.getMessage());
						CmdConsole.this.pw.flush();
					}
					
				}
			}
			
		}.start();
		
	}
	
	/**
	 * ��ʼ��cmd�����
	 */
	private void initCmds() {
		
		for( Class<?> clazz : ClassUtils.getClasses("com.googlecode.greysanatomy.cmd") ) {
			if( clazz.isAnnotationPresent(Cmd.class) ) {
				final Cmd cmd = clazz.getAnnotation(Cmd.class);
				cmdPools.put(cmd.name(), clazz);
			}
			
		}
		
	}
	
	/**
	 * ��������������н�����Cmd
	 * @param line
	 * @throws CmdConsoleException
	 * @return null:�������/��null:���������
	 */
	private BaseCmd parseCmd(String line) throws CmdConsoleException {
		final String[] strs = line.split("\\s");
		final String name = strs[0];
		final Class<?> cmdClass = cmdPools.get(name);
		try {
			
			if( null == cmdClass ) {
				throw new CmdConsoleException(String.format("command '%s' was not found.", name));
			}
			
			// ʵ��������
			final BaseCmd cmd = (BaseCmd)cmdClass.getConstructor().newInstance();
			
			// ��ȡ����
			final Map<String,String> argsMap = parseArgsMap(strs);
			
			// У�鲢���ò���
			checkAndInjectArgs(cmd, name, argsMap);
			
			return cmd;
		}catch(Throwable t) {
//			logger.warn("execute failed.",t);
			if( t instanceof CmdConsoleException ) {
				throw (CmdConsoleException)t;
			}
			throw new CmdConsoleException(String.format("%s execute failed.", name), t);
		}
		
	}
	
	/**
	 * ��ȡ���������
	 * @param strs
	 * @return
	 */
	private Map<String,String> parseArgsMap(String[] strs) {
		final Map<String,String> argsMap = new HashMap<String,String>();
		for( int index=1; index<strs.length; index++ ) {
			final String arg = strs[index];
			// �յģ�������-��ͷ�ģ����ǷǷ��Ĳ���������֮
			if( null == arg || arg.isEmpty() || !arg.startsWith("-") ) {
				continue;
			}
			final int i = arg.indexOf("=");// ȡ�õ�һ���Ⱥţ�Ȼ�����ָ�
			final String argK = i>0?arg.substring(1, i):arg;
			final String argV = i>0?arg.substring(i+1, arg.length()):"";
			argsMap.put(argK, argV);
		}//for
		return argsMap;
	}
	
	/**
	 * У�鲢���ò���
	 * @param cmd
	 * @param cmdname
	 * @param argsMap
	 * @throws CmdConsoleException
	 */
	private void checkAndInjectArgs(BaseCmd cmd, String cmdname, Map<String,String> argsMap) throws CmdConsoleException {
		
		for(Field field : ClassUtils.listFieldsFromClass(cmd.getClass())) {
			if( !field.isAnnotationPresent(CmdArg.class) ) {
				continue;
			}
			final CmdArg cmdArg = field.getAnnotation(CmdArg.class);
			
			if( cmdArg.nullable() && !argsMap.containsKey(cmdArg.name()) ) {
				continue;
			}
			
			final String value = argsMap.get(cmdArg.name());
			if( !cmdArg.nullable() && (null == value || value.trim().isEmpty())) {
				throw new CmdConsoleException(String.format("arg:%s is missed in %s", cmdArg.name(), cmdname));
			}
			
			if( !value.trim().matches(cmdArg.verify()) ) {
				throw new CmdConsoleException(String.format("arg:%s is illegal in %s", cmdArg.name(), cmdname));
			}
			
			final boolean isAccessible = field.isAccessible();
			field.setAccessible(true);
			try {
				if( field.getType().equals(char.class) ) {
					field.setChar(cmd, Character.valueOf(value.charAt(0)));
				} else if( field.getType().equals(double.class) ) {
					field.setDouble(cmd, Double.valueOf(value));
				} else if( field.getType().equals(float.class) ) {
					field.setFloat(cmd, Float.valueOf(value));
				} else if( field.getType().equals(int.class) ) {
					field.setInt(cmd, Integer.valueOf(value));
				} else if( field.getType().equals(long.class) ) {
					field.setLong(cmd, Long.valueOf(value));
				} else if( field.getType().equals(short.class) ) {
					field.setShort(cmd, Short.valueOf(value));
				} else if( field.getType().equals(byte.class) ) {
					field.setByte(cmd, Byte.valueOf(value));
				} else if( field.getType().equals(boolean.class) ) {
					field.setBoolean(cmd, Boolean.valueOf(value));
				} else if( field.getType().equals(Character.class) ) {
					field.setChar(cmd, Character.valueOf(value.charAt(0)));
				} else if( field.getType().equals(Double.class) ) {
					field.setDouble(cmd, Double.valueOf(value));
				} else if( field.getType().equals(Float.class) ) {
					field.setFloat(cmd, Float.valueOf(value));
				} else if( field.getType().equals(Integer.class) ) {
					field.setInt(cmd, Integer.valueOf(value));
				} else if( field.getType().equals(Long.class) ) {
					field.setLong(cmd, Long.valueOf(value));
				} else if( field.getType().equals(Short.class) ) {
					field.setShort(cmd, Short.valueOf(value));
				} else if( field.getType().equals(Byte.class) ) {
					field.setByte(cmd, Byte.valueOf(value));
				} else if( field.getType().equals(Boolean.class) ) {
					field.setBoolean(cmd, Boolean.valueOf(value));
				} else if( field.getType().equals(String.class) ) {
					field.set(cmd, value);
				} else {
					throw new CmdConsoleException(String.format("the arg:%s is unsupport type!", cmdArg.name()));
				}
			} catch(Throwable t){
				if( t instanceof CmdConsoleException ) {
					throw (CmdConsoleException)t;
				} else {
					throw new CmdConsoleException(String.format("the arg:%s is unsupport type!", cmdArg.name()));
				}
			}finally {
				field.setAccessible(isAccessible);
			}
			
		}
		
	}
	
	
	/**
	 * ����������Ϣ
	 * @param respCmd
	 */
	public void printRespCmd(RespCmd respCmd) {
		if( null == respCmd 
				|| null == respCmd.getMessage() 
				|| respCmd.getMessage().trim().isEmpty()) {
			return;
		}
		pw.println(respCmd.getMessage());
		pw.flush();
	}
	
	public static void main(String... args) {
		
		new CmdConsole(new CmdCallback() {
			
			@Override
			public void callback(BaseCmd cmd, CmdConsole console)
					throws CmdConsoleException {
				System.out.println(cmd.getClass());
			}
		});
		
	}
	
}
