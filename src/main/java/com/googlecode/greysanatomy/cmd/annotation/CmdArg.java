package com.googlecode.greysanatomy.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令参数
 * @author vlinux
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CmdArg {

	/**
	 * 在命令行中的参数名称
	 * @return 参数名称
	 */
	public String name();

	/**
	 * 正则表达式校验
	 * @return 返回对参数内容进行校验的正则表达式
	 */
	public String verify() default ".*";
	
	/**
	 * 是否允许为空
	 * @return true:允许为空/false:不允许为空
	 */
	public boolean nullable() default true;
	
}
