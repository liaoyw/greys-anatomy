package com.googlecode.greysanatomy.util;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.CodeSource;

import com.googlecode.greysanatomy.probe.Probes;

public class GaDetailUtils {

	
	private static final String STEP_TAB = "                    ";
	private static final String STEP_FLOW_TAB = "  ";
	private static final String FLOW_TAB = "                  ";
	private static final String NULL = "null";
	
	public static String detail(Class<?> clazz) {
		
		StringBuilder detailSB = new StringBuilder();
		detailSB.append("class info : ").append(getClassName(clazz)).append("\n");
		detailSB.append("--------------------------------------------------------------------------------\n");
		
		CodeSource cs = clazz.getProtectionDomain().getCodeSource(); 
		detailSB.append(format("%15s : %s\n","code-source",null == cs?NULL:cs.getLocation().getFile()));
		detailSB.append(format("%15s : %s\n","name",getClassName(clazz)));
		detailSB.append(format("%15s : %s\n","simple-name",clazz.getSimpleName()));
		detailSB.append(format("%15s : %s\n","modifier",tranModifier(clazz.getModifiers())));
		
		// super-class
		{
			StringBuilder preSB = new StringBuilder();
			Class<?> superClass = clazz.getSuperclass();
			if( null != superClass ) {
				StringBuilder superSB = new StringBuilder(getClassName(superClass)).append("\n");
				while( true ) {
					superClass = superClass.getSuperclass();
					if( null == superClass ) {
						break;
					}
					superSB.append(STEP_TAB).append(preSB.toString()).append("`-->").append(getClassName(superClass)).append("\n");
					preSB.append(STEP_FLOW_TAB);
				}//while
				detailSB.append(format("%15s : %s","super-class",superSB.toString()));
			} else {
				detailSB.append(format("%15s : %s\n","super-class",NULL));
			}
			
		}
		
		// annotation
		{
			StringBuilder annoSB = new StringBuilder();
			Annotation[] annos = clazz.getDeclaredAnnotations();
			if( null != annos && annos.length > 0 ) {
				for( Annotation anno : annos ) {
					annoSB.append(getClassName(anno.annotationType())).append(",");
				}
				if( annoSB.length() > 0 ) {
					annoSB.deleteCharAt(annoSB.length()-1);
				}
			} else {
				annoSB.append(NULL);
			}
			detailSB.append(format("%15s : %s\n","annotation",annoSB.toString()));
		}
		
		// class-loader
		{
			StringBuilder preSB = new StringBuilder();
			StringBuilder loaderSB = new StringBuilder();
			ClassLoader loader = clazz.getClassLoader();
			if( null != loader ) {
				loaderSB.append(loader.toString()).append("\n");
				while( true ) {
					loader = loader.getParent();
					if( null == loader ) {
						break;
					}
					loaderSB.append(STEP_TAB).append(preSB.toString()).append("`-->").append(loader.toString()).append("\n");
					preSB.append(STEP_FLOW_TAB);
				}
			} else {
				loaderSB.append(NULL).append("\n");
			}//if
			detailSB.append(format("%15s : %s","class-loader",loaderSB.toString()));
		}
		
		return detailSB.toString();
		
	}
	
	private static String tranModifier(int mod) {
		StringBuilder sb = new StringBuilder();
		if( Modifier.isAbstract(mod) ) {
			sb.append("abstract,");
		}
		if(Modifier.isFinal(mod)) {
			sb.append("final,");
		}
		if(Modifier.isInterface(mod)) {
			sb.append("interface,");
		}
		if(Modifier.isNative(mod)) {
			sb.append("native,");
		}
		if(Modifier.isPrivate(mod)) {
			sb.append("private,");
		}
		if(Modifier.isProtected(mod)) {
			sb.append("protected,");
		}
		if(Modifier.isPublic(mod)) {
			sb.append("public,");
		}
		if(Modifier.isStatic(mod)) {
			sb.append("static,");
		}
		if(Modifier.isStrict(mod)) {
			sb.append("strict,");
		}
		if(Modifier.isSynchronized(mod)) {
			sb.append("synchronized,");
		}
		if(Modifier.isTransient(mod)) {
			sb.append("transient,");
		}
		if(Modifier.isVolatile(mod)) {
			sb.append("volatile,");
		}
		if( sb.length() > 0 ) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
	public static String detail(Method method) {
		StringBuilder detailSB = new StringBuilder();
		detailSB.append("method info : ").append(format("%s->%s",method.getDeclaringClass().getName(), method.getName())).append("\n");
		detailSB.append("--------------------------------------------------------------------------------\n");

		detailSB.append(format("%15s : %s\n","declaring-class",getClassName(method.getDeclaringClass())));
		detailSB.append(format("%15s : %s\n","modifier",tranModifier(method.getModifiers())));
		detailSB.append(format("%15s : %s\n","name",method.getName()));
		
		// annotation
		{
			StringBuilder annoSB = new StringBuilder();
			Annotation[] annos = method.getDeclaredAnnotations();
			if( null != annos && annos.length > 0 ) {
				for( Annotation anno : annos ) {
					annoSB.append(anno.annotationType().getName()).append(",");
				}
				if( annoSB.length() > 0 ) {
					annoSB.deleteCharAt(annoSB.length()-1);
				}
			} else {
				annoSB.append(NULL);
			}
			detailSB.append(format("%15s : %s\n","annotation",annoSB.toString()));
		}
		
		detailSB.append(format("%15s : %s\n","return-type",getClassName(method.getReturnType())));
		
		// params
		{
			StringBuilder paramSB = new StringBuilder();
			Class<?>[] paramTypes = method.getParameterTypes();
			if( null != paramTypes && paramTypes.length > 0 ) {
				boolean isFirst = true;
				for( Class<?> clazz : paramTypes ) {
					paramSB.append(isFirst?"":FLOW_TAB).append(getClassName(clazz)).append("\n");
					isFirst = false;
				}
				if( paramSB.length() > 0 ) {
					paramSB.deleteCharAt(paramSB.length()-1);
				}
			} else {
				paramSB.append(NULL);
			}
			detailSB.append(format("%15s : %s\n","params",paramSB.toString()));
		}
		
		return detailSB.toString();
	}
	
	private static String getClassName(Class<?> clazz) {
		if( clazz.isArray() ) {
			StringBuilder sb = new StringBuilder(clazz.getName());
			sb.delete(0, 2);
			if( sb.length() > 0 && sb.charAt(sb.length()-1) == ';' ) {
				sb.deleteCharAt(sb.length()-1);
			}
			sb.append("[]");
			return sb.toString();
		} else {
			return clazz.getName();
		}
	}
	
	public static void main(String... args) {

		Class<?> clazz = Probes.class;
		for( Method method : clazz.getDeclaredMethods() ) {
			final String detail = detail(method);
			System.out.println(detail);
		}
		

	}

}
