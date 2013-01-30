package com.googlecode.greysanatomy;

import static com.googlecode.greysanatomy.util.GaReflectUtils.*;
import static com.googlecode.greysanatomy.util.GaStringUtils.*;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

/**
 * ������
 * @author vlinux
 *
 */
public class Configer {

	/*
	 * ����̨���Ӷ˿�
	 */
	private int consolePort = 3658;
	
	/*
	 * �Է�java���̺�
	 */
	private int javaPid;
	
	/*
	 * ���ӳ�ʱʱ��(ms)
	 */
	private long connectTimeout = 6000;
	
	/*
	 * ����̨��ʾ��
	 */
	private String consolePrompt = "ga?>";
	
	public Configer() {
		//
	}
	
	/**
	 * ��Configer����ת��Ϊ�ַ���
	 */
	public String toString() {
		final StringBuilder strSB = new StringBuilder();
		for(Field field : getFileds(Configer.class)) {
			try {
				strSB.append(field.getName()).append("=").append(encode(newString(getFieldValueByField(this, field)))).append(";");
			}catch(Throwable t) {
				//
			}
		}//for
		return strSB.toString();
	}
	
	/**
	 * ��toString������ת��ΪConfiger����
	 * @param toString
	 * @return
	 */
	public static Configer toConfiger(String toString) {
		final Configer configer = new Configer();
		final String[] pvs = StringUtils.split(toString,";");
		for( String pv : pvs ) {
			try {
				final String[] strs = StringUtils.split(pv,"=");
				final String p = strs[0];
				final String v = decode(strs[1]);
				final Field field = getField(Configer.class, p);
				set(field, valueOf(field.getType(), v), configer);
			}catch(Throwable t) {
				//
			}
		}
		return configer;
	}

	/**
	 * ��ȡ����̨�˿�
	 * @return
	 */
	public int getConsolePort() {
		return consolePort;
	}

	/**
	 * ���ÿ���̨�˿�
	 * @param consolePort
	 */
	public void setConsolePort(int consolePort) {
		this.consolePort = consolePort;
	}

	/**
	 * ��ȡĿ��java���̺�
	 * @return
	 */
	public int getJavaPid() {
		return javaPid;
	}

	/**
	 * ����Ŀ��java���̺�
	 * @param javaPid
	 */
	public void setJavaPid(int javaPid) {
		this.javaPid = javaPid;
	}

	/**
	 * ��ȡ���ӳ�ʱʱ��
	 * @return
	 */
	public long getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * �������ӳ�ʱʱ��
	 * @param connectTimeout
	 */
	public void setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * ��ȡ����̨��ʾ��
	 * @return
	 */
	public String getConsolePrompt() {
		return consolePrompt;
	}

	/**
	 * ���ÿ���̨��ʾ��
	 * @param consolePrompt
	 */
	public void setConsolePrompt(String consolePrompt) {
		this.consolePrompt = consolePrompt;
	}
	
}
