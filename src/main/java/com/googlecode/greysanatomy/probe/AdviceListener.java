package com.googlecode.greysanatomy.probe;

/**
 * ̽���������ӿ�
 * @author vlinux
 *
 */
public interface AdviceListener extends JobListener {

	/**
	 * ���ÿ�ʼ
	 * @param advice
	 */
	void onBefore(Advice advice);
	
	/**
	 * ���óɹ�
	 * @param advice
	 */
	void onSuccess(Advice advice);
	
	/**
	 * �����׳��쳣
	 * @param advice
	 */
	void onException(Advice advice);
	
	/**
	 * ���ý���(�ɹ�+�׳��쳣)
	 * @param advice
	 */
	void onFinish(Advice advice);
	
}
