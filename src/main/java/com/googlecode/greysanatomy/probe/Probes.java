package com.googlecode.greysanatomy.probe;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 探测点触发者<br/>
 * 在埋的点中，一共有4种探测点，他们分别对应<br/>
 * fucntion f() 
 * {
 *     // probe:_before()
 *     try {
 *          do something...
 *          // probe:_success()     
 *     } catch(Throwable t) {
 *          // probe:_throws();
 *          throw t;
 *     } finally {
 *          // probe:_finish();
 *     }
 *     
 * }
 * @author vlinux
 *
 */
public class Probes {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy"); 
	private static final String probesClass = "com.googlecode.greysanatomy.probe.Probes";
	
	/**
	 * 序列
	 */
	private static final AtomicInteger idseq = new AtomicInteger();

	/**
	 * 任务
	 * @author vlinux
	 *
	 */
	private static class Job {
		private int id;
		private boolean isAlive;
		private final List<ProbeListener> listeners = new ArrayList<ProbeListener>();
	}
	
	private static final Map<Integer,Job> jobs = new ConcurrentHashMap<Integer, Job>();
	
	
	/**
	 * 注册侦听器
	 * @param listener
	 */
	public static void register(int id, ProbeListener listener) {
		Job job = jobs.get(id);
		if( null != job ) {
			job.listeners.add(listener);
			listener.create();
		}
	}
	
	/**
	 * 创建一个job
	 * @return
	 */
	public static int createJob() {
		final int id = idseq.incrementAndGet();
		Job job = new Job();
		job.id = id;
		job.isAlive = false;
		jobs.put(id, job);
		return id;
	}
	
	/**
	 * 激活一个job
	 * @param id
	 */
	public static void activeJob(int id) {
		Job job = jobs.get(id);
		if( null != job ) {
			job.isAlive = true;
		}
	}
	
	/**
	 * 判断job是否还可以继续工作
	 * @param id
	 * @return
	 */
	public static boolean isJobAlive(int id) {
		Job job = jobs.get(id);
		return null != job && job.isAlive;
	}
	
	/**
	 * 杀死一个job
	 * @param id
	 */
	public static void killJob(int id) {
		Job job = jobs.get(id);
		if( null != job ) {
			job.isAlive = false;
			for(ProbeListener listener : job.listeners) {
				try {
					listener.destroy();
				}catch(Throwable t) {
					logger.warn("destroy listener failed, jobId={}", id, t);
				}
			}
		}
	}
	
	/**
	 * 返回存活的jobId
	 * @return
	 */
	public static List<Integer> listAliveJobIds() {
		final List<Integer> jobIds = new ArrayList<Integer>();
		for(Job job : jobs.values()) {
			if( job.isAlive ) {
				jobIds.add(job.id);
			}
		}
		return jobIds;
	}
	
	/**
	 * 执行前置
	 * @param id
	 * @param targetClass
	 * @param targetMethod
	 * @param targetThis
	 * @param args
	 */
	public static void doBefore(int id, Class<?> targetClass, Method targetMethod, Object targetThis, Object[] args) {
		for( ProbeListener listener : jobs.get(id).listeners ) {
			try {
				Probe p = new Probe(targetClass, targetMethod, targetThis, args, false);
				listener.onBefore(p);
			}catch(Throwable t) {
				logger.warn("error at doBefore", t);
			}
		}
	}
	
	/**
	 * 执行成功
	 * @param id
	 * @param targetClass
	 * @param targetMethod
	 * @param targetThis
	 * @param args
	 * @param returnObj
	 */
	public static void doSuccess(int id, Class<?> targetClass, Method targetMethod, Object targetThis, Object[] args, Object returnObj) {
		for( ProbeListener listener : jobs.get(id).listeners ) {
			try {
				Probe p = new Probe(targetClass, targetMethod, targetThis, args, false);
				p.setReturnObj(returnObj);
				listener.onSuccess(p);
			}catch(Throwable t) {
				logger.warn("error at onSuccess", t);
			}
		}
		doFinish(id, targetClass, targetMethod, targetThis, args, returnObj, null);
	}
	
	/**
	 * 执行异常
	 * @param id
	 * @param targetClass
	 * @param targetMethod
	 * @param targetThis
	 * @param args
	 * @param throwException
	 */
	public static void doException(int id, Class<?> targetClass, Method targetMethod, Object targetThis, Object[] args, Throwable throwException) {
		for( ProbeListener listener : jobs.get(id).listeners ) {
			try {
				Probe p = new Probe(targetClass, targetMethod, targetThis, args, false);
				p.setThrowException(throwException);
				listener.onException(p);
			}catch(Throwable t) {
				logger.warn("error at onException", t);
			}
		}
		doFinish(id, targetClass, targetMethod, targetThis, args, null, throwException);
	}
	
	/**
	 * 执行完成
	 * @param id
	 * @param targetClass
	 * @param targetMethod
	 * @param targetThis
	 * @param args
	 * @param returnObj
	 * @param throwException
	 */
	public static void doFinish(int id, Class<?> targetClass, Method targetMethod, Object targetThis, Object[] args, Object returnObj, Throwable throwException) {
		for( ProbeListener listener : jobs.get(id).listeners ) {
			try {
				Probe p = new Probe(targetClass, targetMethod, targetThis, args, true);
				p.setThrowException(throwException);
				p.setReturnObj(returnObj);
				listener.onFinish(p);
			}catch(Throwable t) {
				logger.warn("error at onFinish", t);
			}
		}
	}
	
	
	private static final Map<String,Class<?>> cacheForGetClassForName = new HashMap<String,Class<?>>();
	
	/**
	 * 获取类信息
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<?> getClassForName(String name) throws ClassNotFoundException {
		if( cacheForGetClassForName.containsKey(name) ) {
			return cacheForGetClassForName.get(name);
		}
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		final Class<?> clazz;
		if( null != loader ) {
			clazz = loader.loadClass(name);
		} else {
			clazz = java.lang.Class.forName(name);
		}//if
		cacheForGetClassForName.put(name, clazz);
		return clazz;
	}
	
	
	private static class GetMethodData {
		private final String className;
		private final String methodName;
		private final Class<?>[] paramTypes;
		private GetMethodData(String className, String methodName,
				Class<?>[] paramTypes) {
			this.className = className;
			this.methodName = methodName;
			this.paramTypes = paramTypes;
		}
		public int hashCode() {
			int hc = className.hashCode() + methodName.hashCode();
			if( null != paramTypes ) {
				for( Class<?> c : paramTypes ) {
					hc += c.hashCode();
				}
			}
			return hc;
		}
		public boolean equals(Object obj) {
			if( null == obj
					|| !(obj instanceof GetMethodData) ) {
				return false;
			}
			
			GetMethodData o = (GetMethodData)obj;
			
			if( !className.equals(o.className)
					|| !methodName.equals(o.methodName)) {
				return false;
			}
			if( null != paramTypes ) {
				if( null == o.paramTypes 
						|| paramTypes.length!=o.paramTypes.length) {
					return false;
				}
				for( int i=0;i<paramTypes.length;i++ ) {
					if( !paramTypes[i].equals(o.paramTypes[i]) ) {
						return false;
					}
				}
			} else {
				if( null != o.paramTypes ) {
					return false;
				}
			}
			return true;
		}
	}
	
	private static final Map<GetMethodData, Method> getMethodForNameCache = new HashMap<GetMethodData, Method>();
	
	public static Method getMethodForName(String className, String methodName, Class<?>... paramTypes) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		final GetMethodData gmd = new GetMethodData(className, methodName, paramTypes);
		if( getMethodForNameCache.containsKey(gmd) ) {
			return getMethodForNameCache.get(gmd);
		}
		final Class<?> clazz = getClassForName(className);
		Method method = clazz.getDeclaredMethod(methodName, paramTypes);
		getMethodForNameCache.put(gmd, method);
		return method;
	}
	
	/**
	 * 埋点探测器
	 * @param id
	 * @param loader
	 * @param cc
	 * @param cm
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 * @throws ClassNotFoundException 
	 */
	public static void mine(int id, ClassLoader loader, CtClass cc, CtMethod cm) throws CannotCompileException, NotFoundException, ClassNotFoundException {
		
		// 抽象方法过滤掉
		if( Modifier.isAbstract(cm.getModifiers()) ) {
			return;
		}
		
		if( Modifier.isStatic(cm.getModifiers()) ) {
			mineForStatic(id,loader, cc,cm);
		} else {
			mineForInstance(id,loader, cc,cm);
		}
	}
	
	/**
	 * 给静态方法埋入探测器
	 * @param id
	 * @param loader
	 * @param cc
	 * @param cm
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 * @throws ClassNotFoundException 
	 */
	private static void mineForStatic(int id, ClassLoader loader, CtClass cc, CtMethod cm) throws CannotCompileException, NotFoundException, ClassNotFoundException {
		final String targetClass = format("%s.getClassForName(\"%s\")", probesClass, cc.getName());
//		final String targetMethod = format("%s.getClassForName(\"%s\").getDeclaredMethod(\"%s\",%s)", probesClass, cc.getName(), cm.getName(), getMethodParamTypes(cm));
		final String targetMethod = format("%s.getMethodForName(\"%s\",\"%s\",%s)", probesClass, cc.getName(), cm.getName(), getMethodParamTypes(cm));
		cm.insertBefore(format("{if(%s.isJobAlive(%s))%s.doBefore(%s,%s,%s,null,$args);}", probesClass, id, probesClass, id, targetClass, targetMethod));
		cm.addCatch(format("{if(%s.isJobAlive(%s))%s.doException(%s,%s,%s,null,$args,$e);throw $e;}", probesClass, id, probesClass, id, targetClass, targetMethod), ClassPool.getDefault().get("java.lang.Throwable"));
		cm.insertAfter(format("{if(%s.isJobAlive(%s))%s.doSuccess(%s,%s,%s,null,$args,($w)$_);}", probesClass, id, probesClass, id, targetClass, targetMethod));
	}
	
	private static final Map<CtMethod,String> cacheForGetMethodParamTypes = new HashMap<CtMethod,String>();

	/**
	 * 获取CtMethod所封装的参数信息
	 * @param cm
	 * @return
	 * @throws NotFoundException
	 */
	private static String getMethodParamTypes(CtMethod cm) throws NotFoundException {
		if( cacheForGetMethodParamTypes.containsKey(cm) ) {
			return cacheForGetMethodParamTypes.get(cm);
		}
		StringBuilder sb = new StringBuilder();
		CtClass[] ccs = cm.getParameterTypes();
		final String returnStr;
		if( null != ccs && ccs.length > 0) {
			for( CtClass cc : ccs ) {
				
				String name = cc.getName();
				if( cc.isArray() ) {
					sb.append(format("%s.class", name));
				}else if( name.equals("long") ) {
					sb.append(name+".class");
				}else if( name.equals("int") ) {
					sb.append(name+".class");
				}else if( name.equals("double") ) {
					sb.append(name+".class");
				}else if( name.equals("float") ) {
					sb.append(name+".class");
				}else if( name.equals("char") ) {
					sb.append(name+".class");
				}else if( name.equals("byte") ) {
					sb.append(name+".class");
				}else if( name.equals("short") ) {
					sb.append(name+".class");
				}else if( name.equals("boolean") ) {
					sb.append(name+".class");
				}else {
					sb.append(format("%s.getClassForName(\"%s\")", probesClass, name));
				}//if
				
				sb.append(",");
				
			}
			sb.deleteCharAt(sb.length()-1);
			returnStr = format("new Class[]{%s}", sb.toString());
		} else {
			returnStr = "null";
		}
		cacheForGetMethodParamTypes.put(cm, returnStr);
		return returnStr;
	}
	
	/**
	 * 给对象方法埋入探测器
	 * @param id
	 * @param loader
	 * @param cc
	 * @param cm
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 * @throws ClassNotFoundException 
	 */
	private static void mineForInstance(int id, ClassLoader loader, CtClass cc, CtMethod cm) throws CannotCompileException, NotFoundException, ClassNotFoundException {
		final String targetClass = format("%s.getClassForName(\"%s\")", probesClass, cc.getName());
//		final String targetMethod = format("%s.getClassForName(\"%s\").getDeclaredMethod(\"%s\",%s)", probesClass, cc.getName(), cm.getName(), getMethodParamTypes(cm));
		final String targetMethod = format("%s.getMethodForName(\"%s\",\"%s\",%s)", probesClass, cc.getName(), cm.getName(), getMethodParamTypes(cm));
		cm.insertBefore(format("{if(%s.isJobAlive(%s))%s.doBefore(%s,%s,%s,this,$args);}", probesClass, id, probesClass, id, targetClass, targetMethod));
		cm.addCatch(format("{if(%s.isJobAlive(%s))%s.doException(%s,%s,%s,this,$args,$e);throw $e;}", probesClass, id, probesClass, id, targetClass, targetMethod), ClassPool.getDefault().get("java.lang.Throwable"));
		cm.insertAfter(format("{if(%s.isJobAlive(%s))%s.doSuccess(%s,%s,%s,this,$args,($w)$_);}", probesClass, id, probesClass, id, targetClass, targetMethod));
	}
	
}
