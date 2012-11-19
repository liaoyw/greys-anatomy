package com.googlecode.greysanatomy.console.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ����У��
 * @author vlinux
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ArgVerifier {

	/**
	 * ��֤�õ�������ʽ
	 * @return
	 */
	public String regex();
	
	/**
	 * ��֤ʧ��ʱ�Ĵ�����ʾ
	 * @return
	 */
	public String description();
	
}
