package com.googlecode.greysanatomy.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令名称
 * @author vlinux
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cmd {

	/**
	 * 指定命令的名称<br/>
	 * @return 返回命令的名称
	 */
	public String name();
	
}
