package com.googlecode.greysanatomy.cmd;

import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 命令
 * @author vlinux
 *
 */
public abstract class BaseCmd implements Serializable {

	private static final long serialVersionUID = -7384260558741288245L;
	
	/*
	 * 命令序列
	 */
	private static final AtomicLong seq = new AtomicLong();
	
	/*
	 * 命令ID
	 */
	private final long id;
	
	/*
	 * 在Agent端的inst信息 
	 */
	private transient Instrumentation inst;
	
	/**
	 * 构造函数<br/>
	 * 用于构造CMD回复
	 * @param id
	 */
	public BaseCmd(long id) {
		this.id = id;
	}
	
	/**
	 * 构造函数<br/>
	 * 用于构造CMD请求
	 */
	public BaseCmd() {
		this.id = seq.incrementAndGet();
	}

	/**
	 * 获取命令序号
	 * @return 返回本次命令的序号
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
