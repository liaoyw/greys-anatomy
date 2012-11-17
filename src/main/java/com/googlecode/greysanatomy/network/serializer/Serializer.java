package com.googlecode.greysanatomy.network.serializer;

import java.io.Serializable;

/**
 * ���л�
 * @author vlinux
 *
 */
public interface Serializer {

	/**
	 * ���л�
	 * @param serializable
	 * @return
	 * @throws SerializationException
	 */
	<T extends Serializable> byte[] encode(T serializable) throws SerializationException;
	
	/**
	 * �����л�
	 * @param bytes
	 * @return
	 * @throws SerializationException
	 */
	<T extends Serializable> T decode(byte[] bytes) throws SerializationException;
	
}
