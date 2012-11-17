package com.googlecode.greysanatomy.probe;

import java.lang.reflect.Method;

/**
 * 探测点
 * 
 * @author vlinux
 * 
 */
public class Probe {

	private final Class<?> targetClass; // 目标类
	private final Method targetMethod; 	// 目标方法
	private final Object[] parameters; 	// 调用参数
	private final Object targetThis; 	// 目标实例，如果是代理的静态类，则此值为null
	private final boolean isFinished;	// 是否到doFinish方法
	
	private Object returnObj; 			// 返回值，如果目标方法以抛异常的形式结束，则此值为null
	private Throwable throwException; 	// 抛出异常，如果目标方法以正常方式结束，则此值为null

	/**
	 * 探测器构造函数
	 * @param targetClass
	 * @param targetMethod
	 * @param targetThis
	 * @param parameters
	 * @param isFinished
	 */
	public Probe(Class<?> targetClass, Method targetMethod, Object targetThis, Object[] parameters, boolean isFinished) {
		this.targetClass = targetClass;
		this.targetMethod = targetMethod;
		this.targetThis = targetThis;
		this.parameters = parameters;
		this.isFinished = isFinished;
	}

	/**
	 * 是否以抛出异常结束
	 * @return true:以抛异常形式结束/false:以非抛异常形式结束，或尚未结束
	 */
	public boolean isThrowException() {
		return isFinished() && null != throwException;
	}
	
	/**
	 * 是否以正常返回结束
	 * @return true:以正常返回形式结束/false:以非正常返回形式结束，或尚未结束
	 */
	public boolean isReturn() {
		return isFinished() && !isThrowException();
	}
	
	/**
	 * 是否已经结束
	 * @return true:已经结束/false:尚未结束
	 */
	public boolean isFinished() {
		return isFinished;
	}
	
	public Object getTargetThis() {
		return targetThis;
	}

	public Object getReturnObj() {
		return returnObj;
	}

	public void setReturnObj(Object returnObj) {
		this.returnObj = returnObj;
	}

	public Throwable getThrowException() {
		return throwException;
	}

	public void setThrowException(Throwable throwException) {
		this.throwException = throwException;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public Method getTargetMethod() {
		return targetMethod;
	}

	public Object[] getParameters() {
		return parameters;
	}

}
