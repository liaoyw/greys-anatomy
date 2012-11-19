package com.googlecode.greysanatomy.probe;

import java.lang.reflect.Method;

/**
 * ̽���
 * 
 * @author vlinux
 * 
 */
public class Probe {

	private final Class<?> targetClass; // Ŀ����
	private final Method targetMethod; 	// Ŀ�귽��
	private final Object[] parameters; 	// ���ò���
	private final Object targetThis; 	// Ŀ��ʵ��������Ǵ���ľ�̬�࣬���ֵΪnull
	private final boolean isFinished;	// �Ƿ�doFinish����
	
	private Object returnObj; 			// ����ֵ�����Ŀ�귽�������쳣����ʽ���������ֵΪnull
	private Throwable throwException; 	// �׳��쳣�����Ŀ�귽����������ʽ���������ֵΪnull

	/**
	 * ̽�������캯��
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
	 * �Ƿ����׳��쳣����
	 * @return true:�����쳣��ʽ����/false:�Է����쳣��ʽ����������δ����
	 */
	public boolean isThrowException() {
		return isFinished() && null != throwException;
	}
	
	/**
	 * �Ƿ����������ؽ���
	 * @return true:������������ʽ����/false:�Է�����������ʽ����������δ����
	 */
	public boolean isReturn() {
		return isFinished() && !isThrowException();
	}
	
	/**
	 * �Ƿ��Ѿ�����
	 * @return true:�Ѿ�����/false:��δ����
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
