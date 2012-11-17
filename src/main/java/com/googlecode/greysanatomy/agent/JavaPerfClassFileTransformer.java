package com.googlecode.greysanatomy.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.probe.ProbeListener;
import com.googlecode.greysanatomy.probe.Probes;
import com.googlecode.greysanatomy.util.ClassUtils;

public class JavaPerfClassFileTransformer implements ClassFileTransformer {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");

	private final String perfClzRegex;
	private final String perfMthRegex;
	private final int id;
	private final List<CtMethod> modifiedMethods;

	private JavaPerfClassFileTransformer(
			final String perfClzRegex,
			final String perfMthRegex, final ProbeListener listener, final List<CtMethod> modifiedMethods) {
		this.perfClzRegex = perfClzRegex;
		this.perfMthRegex = perfMthRegex;
		this.modifiedMethods = modifiedMethods;
		
		id = Probes.createJob();
		Probes.register(id, listener);
	}

	@Override
	public byte[] transform(final ClassLoader loader, String classNameForFilepath,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer)
			throws IllegalClassFormatException {
		
		final String className = ClassUtils.toClassPath(classNameForFilepath);
		if( !className.matches(perfClzRegex)) {
			return null;
		}
		
		final ClassPool cp = new ClassPool(null);
		cp.insertClassPath(new LoaderClassPath(loader));
//		cp.childFirstLookup = true;
		
		logger.info("transform {}, cp.loader={} loader={}.", new Object[]{className, cp.getClassLoader(), loader});
		
		CtClass cc = null;
		byte[] datas;
		try {
			cc = cp.getCtClass(className);
			cc.defrost();
			final CtMethod[] cms = cc.getDeclaredMethods();
			for( int index=0; index<cms.length; index++ ) {
				CtMethod cm = cms[index];
				if( cm.getName().matches(perfMthRegex) ) {
					modifiedMethods.add(cm);
					Probes.mine(id, loader, cc, cm);
				}
			}//for
			datas = cc.toBytecode();
		} catch (Exception e) {
			logger.warn("transform failed!", e);
			datas = null;
		} finally {
			if( null != cc ) {
				cc.freeze();
			}
		}
		
		return datas;
	}
	
	
	/**
	 * 渲染结果
	 * @author vlinux
	 *
	 */
	public static class TransformResult {
		
		private final int id;
		private final List<Class<?>> modifiedClasses;
		private final List<CtMethod> modifiedMethods;
		
		private TransformResult(int id, final List<Class<?>> modifiedClasses, final List<CtMethod> modifiedMethods) {
			this.id = id;
			this.modifiedClasses = new ArrayList<Class<?>>(modifiedClasses);
			this.modifiedMethods = new ArrayList<CtMethod>(modifiedMethods);
		}

		public List<Class<?>> getModifiedClasses() {
			return modifiedClasses;
		}
		
		public List<CtMethod> getModifiedMethods() {
			return modifiedMethods;
		}

		public int getId() {
			return id;
		}
		
	}

	/**
	 * 对类进行形变
	 * @param instrumentation
	 * @param perfClzRegex
	 * @param perfMthRegex
	 * @param listener
	 * @return
	 * @throws UnmodifiableClassException
	 */
	public static TransformResult transform(final Instrumentation instrumentation, 
			final String perfClzRegex, 
			final String perfMthRegex, 
			final ProbeListener listener) throws UnmodifiableClassException {
		
		final List<CtMethod> modifiedMethods = new ArrayList<CtMethod>();
		JavaPerfClassFileTransformer jcft = new JavaPerfClassFileTransformer(perfClzRegex, perfMthRegex, listener, modifiedMethods);
		instrumentation.addTransformer(jcft,true);
		final List<Class<?>> modifiedClasses = new ArrayList<Class<?>>();
		for( Class<?> clazz : instrumentation.getAllLoadedClasses() ) {
			if( clazz.getName().matches(perfClzRegex) ) {
				modifiedClasses.add(clazz);
			}
		}
		try {
			instrumentation.retransformClasses(modifiedClasses.toArray(new Class[0]));
		}finally {
			instrumentation.removeTransformer(jcft);
		}
		
		return new TransformResult(jcft.id, modifiedClasses, modifiedMethods);
		
	}
	
	
}
