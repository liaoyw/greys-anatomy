package com.googlecode.greysanatomy.console.network.serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ���л�����
 * @author vlinux
 *
 */
public class SerializerFactory {

	private static final Map<String, Serializer> serializers = new ConcurrentHashMap<String, Serializer>();
	
	/**
	 * Java���л���ʽ
	 */
	public static final String SERIALIZER_NAME_JAVA = "java";
	
	/**
	 * ע�����л���ʽ
	 * @param name
	 * @param serializer
	 */
	public static void register(String name, Serializer serializer) {
		serializers.put(name, serializer);
	}
	
	static {
		
		// ע��java���л��ķ�ʽ
		register(SERIALIZER_NAME_JAVA, new JavaSerializer());
		
	}
	
	/**
	 * ��ȡ���л�������
	 * @return
	 */
	public static Serializer getInstance() {
		return serializers.get(SERIALIZER_NAME_JAVA);
	}
	
}
