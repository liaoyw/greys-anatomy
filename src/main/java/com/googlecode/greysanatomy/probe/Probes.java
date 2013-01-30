package com.googlecode.greysanatomy.probe;

import static com.googlecode.greysanatomy.probe.ProbeJobs.*;
import static java.lang.String.format;
import static javassist.Modifier.isAbstract;
import static javassist.Modifier.isInterface;
import static javassist.Modifier.isStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.probe.Advice.Target;
import com.googlecode.greysanatomy.probe.Advice.TargetBehavior;
import com.googlecode.greysanatomy.probe.Advice.TargetConstructor;
import com.googlecode.greysanatomy.probe.Advice.TargetMethod;
import com.googlecode.greysanatomy.util.GaCheckUtils;

/**
 * ̽��㴥����<br/>
 * ����ĵ��У�һ����4��̽��㣬���Ƿֱ��Ӧ<br/>
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
	
	private static final String jobsClass = "com.googlecode.greysanatomy.probe.ProbeJobs";
	private static final String probesClass = "com.googlecode.greysanatomy.probe.Probes";
	
	private static final Map<String,Class<?>> cacheForGetClassByName = new ConcurrentHashMap<String,Class<?>>();
	private static final Map<GetBehaviorKey, Method> cacheForGetMethodByName = new ConcurrentHashMap<GetBehaviorKey, Method>();
	private static final Map<GetBehaviorKey, Constructor<?>> cacheForGetConstructorByParamTypes = new ConcurrentHashMap<GetBehaviorKey, Constructor<?>>();
	
	
	/**
	 * ���ݴ���Ĳ����������ղ���Behaveior
	 * @param targetConstructor
	 * @param targetMethod
	 * @return
	 */
	private static TargetBehavior newTargetBehavior(Constructor<?> targetConstructor, Method targetMethod) {
		if( null != targetConstructor ) {
			return new TargetConstructor(targetConstructor);
		} else {
			return new TargetMethod(targetMethod);
		}
	}
	
	/**
	 * ����Target
	 * @param targetClass
	 * @param targetConstructor
	 * @param targetMethod
	 * @param targetThis
	 * @return
	 */
	private static Target newTarget(Class<?> targetClass, Constructor<?> targetConstructor, Method targetMethod, Object targetThis) {
		return new Target(targetClass, newTargetBehavior(targetConstructor, targetMethod), targetThis);
	}
	
	/**
	 * ִ��ǰ��
	 * @param id
	 * @param targetClass
	 * @param targetConstructor
	 * @param targetMethod
	 * @param targetThis
	 * @param args
	 */
	public static void doBefore(String id, Class<?> targetClass, Constructor<?> targetConstructor, Method targetMethod, Object targetThis, Object[] args) {
		if( isListener(id, AdviceListener.class) ) {
			try {
				Advice p = new Advice(newTarget(targetClass, targetConstructor, targetMethod, targetThis), args, false);
				((AdviceListener)getJobListeners(id)).onBefore(p);
			}catch(Throwable t) {
				logger.warn("error at doBefore", t);
			}
		}
	}
	
	/**
	 * ִ�гɹ�
	 * @param id
	 * @param targetClass
	 * @param targetConstructor
	 * @param targetMethod
	 * @param targetThis
	 * @param args
	 * @param returnObj
	 */
	public static void doSuccess(String id, Class<?> targetClass, Constructor<?> targetConstructor, Method targetMethod, Object targetThis, Object[] args, Object returnObj) {
		if( isListener(id, AdviceListener.class) ) {
			try {
				Advice p = new Advice(newTarget(targetClass, targetConstructor, targetMethod, targetThis), args, false);
				p.setReturnObj(returnObj);
				((AdviceListener)getJobListeners(id)).onSuccess(p);
			}catch(Throwable t) {
				logger.warn("error at onSuccess", t);
			}
			doFinish(id, targetClass, targetConstructor, targetMethod, targetThis, args, returnObj, null);
		}
		
	}
	
	/**
	 * ִ���쳣
	 * @param id
	 * @param targetClass
	 * @param targetConstructor
	 * @param targetMethod
	 * @param targetThis
	 * @param args
	 * @param throwException
	 */
	public static void doException(String id, Class<?> targetClass, Constructor<?> targetConstructor, Method targetMethod, Object targetThis, Object[] args, Throwable throwException) {
		if( isListener(id, AdviceListener.class) ) {
			try {
				Advice p = new Advice(newTarget(targetClass, targetConstructor, targetMethod, targetThis), args, false);
				p.setThrowException(throwException);
				((AdviceListener)getJobListeners(id)).onException(p);
			}catch(Throwable t) {
				logger.warn("error at onException", t);
			}
			doFinish(id, targetClass, targetConstructor, targetMethod, targetThis, args, null, throwException);
		}
		
	}
	
	/**
	 * ִ�����
	 * @param id
	 * @param targetClass
	 * @Param targetConstructor
	 * @param targetMethod
	 * @param targetThis
	 * @param args
	 * @param returnObj
	 * @param throwException
	 */
	public static void doFinish(String id, Class<?> targetClass, Constructor<?> targetConstructor, Method targetMethod, Object targetThis, Object[] args, Object returnObj, Throwable throwException) {
		if( isListener(id, AdviceListener.class) ) {
			try {
				Advice p = new Advice(newTarget(targetClass, targetConstructor, targetMethod, targetThis), args, true);
				p.setThrowException(throwException);
				p.setReturnObj(returnObj);
				((AdviceListener)getJobListeners(id)).onFinish(p);
			}catch(Throwable t) {
				logger.warn("error at onFinish", t);
			}
		}
	}
	
	
	/**
	 * ��ȡ����Ϣ
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<?> getClassByName(String name) throws ClassNotFoundException {
		if( cacheForGetClassByName.containsKey(name) ) {
			return cacheForGetClassByName.get(name);
		}
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		final Class<?> clazz;
		if( null != loader ) {
			clazz = loader.loadClass(name);
		} else {
			clazz = java.lang.Class.forName(name);
		}//if
		cacheForGetClassByName.put(name, clazz);
		return clazz;
	}
	
	/**
	 * ��ȡ��Ϊ�Ļ���Key
	 * @author vlinux
	 *
	 */
	private static class GetBehaviorKey {
		
		private final String className;
		private final String behaviorName;
		private final Class<?>[] paramTypes;
		
		private GetBehaviorKey(String className, String behaviorName, Class<?>[] paramTypes) {
			this.className = className;
			this.behaviorName = behaviorName;
			this.paramTypes = paramTypes;
		}
		public int hashCode() {
			int hc = className.hashCode() + behaviorName.hashCode();
			if( null != paramTypes ) {
				for( Class<?> c : paramTypes ) {
					hc += c.hashCode();
				}
			}
			return hc;
		}
		public boolean equals(Object obj) {
			if( null == obj
					|| !(obj instanceof GetBehaviorKey) ) {
				return false;
			}
			
			GetBehaviorKey o = (GetBehaviorKey)obj;
			
			if( !className.equals(o.className)
					|| !behaviorName.equals(o.behaviorName)) {
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
	
	/**
	 * ͨ���������Ͳ����б��ȡ����
	 * @param className
	 * @param methodName
	 * @param paramTypes
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Method getMethodByNameAndParamTypes(String className, String methodName, Class<?>... paramTypes) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		final GetBehaviorKey gmd = new GetBehaviorKey(className, methodName, paramTypes);
		if( cacheForGetMethodByName.containsKey(gmd) ) {
			return cacheForGetMethodByName.get(gmd);
		}
		final Class<?> clazz = getClassByName(className);
		Method method = clazz.getDeclaredMethod(methodName, paramTypes);
		cacheForGetMethodByName.put(gmd, method);
		return method;
	}
	
	/**
	 * ͨ�������б��ȡ���캯��
	 * @param className
	 * @param paramTypes
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Constructor<?> getConstructorByParamTypes(String className, Class<?>... paramTypes) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		final GetBehaviorKey key = new GetBehaviorKey(className, "<init>", paramTypes);
		if( cacheForGetConstructorByParamTypes.containsKey(key) ) {
			return cacheForGetConstructorByParamTypes.get(key);
		}
		final Class<?> clazz = getClassByName(className);
		final Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
		cacheForGetConstructorByParamTypes.put(key, constructor);
		return constructor;
	}
	
	/**
	 * ��ȡCtBehavior����װ�Ĳ�����Ϣ,�ַ�����,����javassist
	 * @param cb
	 * @return
	 * @throws NotFoundException
	 */
	private static String toJavassistStringParamTypes(CtBehavior cb) throws NotFoundException {
		StringBuilder sb = new StringBuilder();
		CtClass[] ccs = cb.getParameterTypes();
		final String returnStr;
		if( null != ccs && ccs.length > 0) {
			for( CtClass cc : ccs ) {
				
				String name = cc.getName();
				if( cc.isArray() 
						|| GaCheckUtils.isIn(name, "long","int","double","float","char","byte","short","boolean") ) {
					sb.append(name).append(".class");
				} else {
					sb.append(format("%s.getClassByName(\"%s\")", probesClass, name));
				}//if
				
				sb.append(",");
				
			}
			sb.deleteCharAt(sb.length()-1);
			returnStr = format("new Class[]{%s}", sb.toString());
		} else {
			returnStr = "null";
		}
		return returnStr;
	}
	
	/**
	 * �Ƿ���˵���ǰ̽���Ŀ��
	 * @param cc
	 * @param cb
	 * @return
	 */
	private static boolean isIngore(CtClass cc, CtBehavior cb) {
		
		final int ccMod = cc.getModifiers();
		final int cbMod = cb.getModifiers();
		
		// ���˵��ӿ�
		if( isInterface(ccMod) ) {
			return true;
		}
				
		// ���˵����󷽷�
		if( isAbstract(cbMod) ) {
			return true;
		}
		
		// ���˵��Լ�������ݹ����
		if( cc.getName().startsWith("com.googlecode.greysanatomy.") ) {
			return true;
		}
		
		return false;
		
	}
	
	
	/**
	 * ���̽����
	 * @param id
	 * @param loader
	 * @param cc
	 * @param cm
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 * @throws ClassNotFoundException 
	 */
	public static void mine(String id, ClassLoader loader, CtClass cc, CtBehavior cb) throws CannotCompileException, NotFoundException, ClassNotFoundException {
		
		if( isIngore(cc, cb) ) {
			return;
		}
		
		// Ŀ����
		final String javassistClass = format("(%s.getClassByName(\"%s\"))", 
				probesClass, 
				cc.getName());
		
		// Ŀ�귽��
		final String javassistMethod = cb.getMethodInfo().isMethod() 
				? format("%s.getMethodByNameAndParamTypes(\"%s\",\"%s\",%s)", 
						probesClass, 
						cc.getName(), 
						cb.getMethodInfo().getName(), 
						toJavassistStringParamTypes(cb))
				: "null";
		
		// Ŀ�깹�캯��
		final String javassistConstructor = cb.getMethodInfo().isConstructor() 
				? format("%s.getConstructorByParamTypes(\"%s\",%s)", 
						probesClass, 
						cc.getName(), 
						toJavassistStringParamTypes(cb))
				: "null";
		
		// Ŀ��ʵ��,����Ǿ�̬��������Ϊnull
		final String javassistThis = isStatic(cb.getModifiers()) ? "null" : "this";
		
		// ���֪ͨ
		if( isListener(id, AdviceListener.class) ) {
			// ���캯���������ǲ�����insertBefore��,���Թ��캯����before������doCache��
			if( cb.getMethodInfo().isMethod() ) {
				mineProbeForMethod(cb, id, javassistClass, javassistConstructor, javassistMethod, javassistThis);
			} else if( cb.getMethodInfo().isConstructor() ) {
				mineProbeForConstructor(cb, id, javassistClass, javassistConstructor, javassistMethod, javassistThis);
			}
		}
		
	}
	
	/**
	 * �����캯�����
	 * @param cb
	 * @param id
	 * @param javassistClass
	 * @param javassistConstructor
	 * @param javassistMethod
	 * @param javassistThis
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	private static void mineProbeForConstructor(CtBehavior cb, String id, String javassistClass, String javassistConstructor, String javassistMethod, String javassistThis) throws CannotCompileException, NotFoundException {
		cb.addCatch(format("{if(%s.isJobAlive(%s)){%s.doBefore(%s,%s,%s,%s,%s,$args);%s.doException(%s,%s,%s,%s,%s,$args,$e);}throw $e;}", 
				jobsClass, id, 
				probesClass, id, javassistClass, javassistConstructor, javassistMethod, javassistThis, 
				probesClass, id, javassistClass, javassistConstructor, javassistMethod, javassistThis), 
			ClassPool.getDefault().get("java.lang.Throwable"));
		cb.insertAfter(format("{if(%s.isJobAlive(%s)){%s.doBefore(%s,%s,%s,%s,%s,$args);%s.doSuccess(%s,%s,%s,%s,%s,$args,($w)$_);}}", 
				jobsClass, id, 
				probesClass, id, javassistClass, javassistConstructor, javassistMethod, javassistThis,
				probesClass, id, javassistClass, javassistConstructor, javassistMethod, javassistThis));
	}
	
	/**
	 * �����������
	 * @param cb
	 * @param id
	 * @param javassistClass
	 * @param javassistConstructor
	 * @param javassistMethod
	 * @param javassistThis
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	private static void mineProbeForMethod(CtBehavior cb, String id, String javassistClass, String javassistConstructor, String javassistMethod, String javassistThis) throws CannotCompileException, NotFoundException {
		cb.insertBefore(format("{if(%s.isJobAlive(%s))%s.doBefore(%s,%s,%s,%s,%s,$args);}", 
				jobsClass, id, probesClass, id, javassistClass, javassistConstructor, javassistMethod, javassistThis));
		cb.addCatch(format("{if(%s.isJobAlive(%s))%s.doException(%s,%s,%s,%s,%s,$args,$e);throw $e;}", 
				jobsClass, id, probesClass, id, javassistClass, javassistConstructor, javassistMethod, javassistThis), 
			ClassPool.getDefault().get("java.lang.Throwable"));
		cb.insertAfter(format("{if(%s.isJobAlive(%s))%s.doSuccess(%s,%s,%s,%s,%s,$args,($w)$_);}", 
				jobsClass, id, probesClass, id, javassistClass, javassistConstructor, javassistMethod, javassistThis));
	}
	
}
