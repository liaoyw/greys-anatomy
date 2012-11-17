package com.googlecode.greysanatomy.cmd;

import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ����
 * @author vlinux
 *
 */
public abstract class BaseCmd implements Serializable {

	private static final long serialVersionUID = -7384260558741288245L;
	
	/*
	 * ��������
	 */
	private static final AtomicLong seq = new AtomicLong();
	
	/*
	 * ����ID
	 */
	private final long id;
	
	/*
	 * ��Agent�˵�inst��Ϣ 
	 */
	private transient Instrumentation inst;
	
	/**
	 * ���캯��<br/>
	 * ���ڹ���CMD�ظ�
	 * @param id
	 */
	public BaseCmd(long id) {
		this.id = id;
	}
	
	/**
	 * ���캯��<br/>
	 * ���ڹ���CMD����
	 */
	public BaseCmd() {
		this.id = seq.incrementAndGet();
	}

	/**
	 * ��ȡ�������
	 * @return ���ر�����������
	 */
	public long getId() {
		return id;
	}

	public Instrumentation getInst() {
		return inst;
	}

	public void setInst(Instrumentation inst) {
		this.inst = inst;
	}

}
