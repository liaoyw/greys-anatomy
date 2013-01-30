package com.googlecode.greysanatomy.console.network.coder;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ����׷����Ϣ
 * @author vlinux
 *
 */
public class CmdTracer implements Serializable{

	private static final long serialVersionUID = -7415043715453515498L;

	/*
	 * ׷������(client-serverΨһ)
	 */
	private static transient final AtomicLong seq = new AtomicLong();
	
	/*
	 * ����׷������
	 */
	private final long id;
	
	/**
	 * ��������ReqCmd
	 */
	protected CmdTracer() {
		id = seq.incrementAndGet();
	}
	
	/**
	 * ��������RespCmd
	 * @param id
	 */
	protected CmdTracer(long id) {
		this.id = id;
	}

	
	/**
	 * ��ȡ����׷������
	 * @return
	 */
	public long getId() {
		return id;
	}
	
}
