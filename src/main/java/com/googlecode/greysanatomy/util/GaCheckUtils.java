package com.googlecode.greysanatomy.util;

/**
 * �쳵������
 * @author vlinux
 *
 */
public class GaCheckUtils {

	/**
	 * �ж�ĳ��ֵ�Ƿ���ĳƬֵ��Χ֮��
	 * @param <E>
	 * @param e
	 * @param collections
	 * @return
	 */
	public static <E> boolean isIn(E e, E... collections ) {
		if( null == collections || collections.length == 0 ) {
			return false;
		}//if
		for( E ce : collections ) {
			if( (null == e && null == ce)
					|| (null != e && null != ce && e.equals(ce)) ) {
				return true;
			}//if
		}//for
		return false;
	}

	/**
	 * ��Сд�޹ص��ж�ĳ���ַ����Ƿ���ĳƬ�ַ���ֵ��Χ֮��
	 * @param s
	 * @param strs
	 * @return
	 */
	public static boolean isInIgnoreCase(String s, String... strs) {
		if( null == strs || strs.length == 0 ) {
			return false;
		}//if
		for( String str : strs ) {
			if( (null == str && null == s)
					|| (null != s && null != str && str.equalsIgnoreCase(s)) ) {
				return true;
			}//if
		}//for
		return false;
	}
	
}
