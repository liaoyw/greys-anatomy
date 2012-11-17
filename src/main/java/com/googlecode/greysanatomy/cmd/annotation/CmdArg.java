package com.googlecode.greysanatomy.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * �������
 * @author vlinux
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CmdArg {

	/**
	 * ���������еĲ�������
	 * @return ��������
	 */
	public String name();

	/**
	 * ������ʽУ��
	 * @return ���ضԲ������ݽ���У���������ʽ
	 */
	public String verify() default ".*";
	
	/**
	 * �Ƿ�����Ϊ��
	 * @return true:����Ϊ��/false:������Ϊ��
	 */
	public boolean nullable() default true;
	
}
