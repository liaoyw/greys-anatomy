package com.googlecode.greysanatomy.console.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Arg {

	/**
	 * ���������еĲ�������
	 * @return ��������
	 */
	public String name();
	
	/**
	 * �Ƿ����
	 * @return
	 */
	public boolean isRequired() default true;
	
	/**
	 * ����У��
	 * @return
	 */
	public ArgVerifier[] verify() default {};
	
}
