package com.googlecode.greysanatomy.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

/**
 * 字符串操作工具类
 * @author vlinux
 *
 */
public class GaStringUtils {

	public static final String LINE = "---------------------------------------------------------------\n";
	
	/**
	 * 进行URL-ENCODE
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encode(String str) throws UnsupportedEncodingException {
		if( StringUtils.isBlank(str) ) {
			return StringUtils.EMPTY;
		}
		return URLEncoder.encode(str, "utf-8");
	}
	
	/**
	 * 进行URL-DECODE
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String decode(String str) throws UnsupportedEncodingException {
		if( StringUtils.isBlank(str) ) {
			return StringUtils.EMPTY;
		}
		return URLDecoder.decode(str, "utf-8");
	}

	/**
	 * 创建一个字符串
	 * @param obj
	 * @return
	 */
	public static String newString(Object obj) {
		if( null == obj ) {
			return StringUtils.EMPTY;
		}
		return obj.toString();
	}
	
	/**
	 * 展示logo
	 * @return
	 */
	public static String getLogo() {
		final StringBuilder logoSB = new StringBuilder();
		final Scanner scanner = new Scanner(Object.class.getResourceAsStream("/com/googlecode/greysanatomy/res/logo.txt"));
		while( scanner.hasNextLine() ) {
			logoSB.append(scanner.nextLine()).append("\n");
		}
		return logoSB.toString();
	}
	
}
