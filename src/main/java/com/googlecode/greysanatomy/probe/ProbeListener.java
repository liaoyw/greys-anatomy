package com.googlecode.greysanatomy.probe;

/**
 * ̽���������ӿ�
 * @author vlinux
 *
 */
public interface ProbeListener {

	/**
	 * ����������
	 */
	void create();
	
	/**
	 * ����������
	 */
	void destroy();
	
	/**
	 * ���ÿ�ʼ
	 * @param p
	 */
	void onBefore(Probe p);
	
	/**
	 * ���óɹ�
	 * @param p
	 */
	void onSuccess(Probe p);
	
	/**
	 * �����׳��쳣
	 * @param p
	 */
	void onException(Probe p);
	
	/**
	 * ���ý���(�ɹ�+�׳��쳣)
	 * @param p
	 */
	void onFinish(Probe p);
	
}
