package com.googlecode.greysanatomy.console.network.coder;

/**
 * ����ͨѶЭ��
 * 
 * @author vlinux
 * 
 */
public final class Protocol {

	/**
	 * Э��ħ��
	 */
	public static final short MAGIC = 0x0c9f;

	/**
	 * Э������:cmd
	 */
	public static byte TYPE_CMD = 0x01;

	/**
	 * Э������:����
	 */
	public static byte TYPE_HEARTBEAT = 0x02;

	private byte type;		//����
	private int length;		//���ݶγ���
	private byte[] datas;	//���ݶ�

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getDatas() {
		return datas;
	}

	public void setDatas(byte[] datas) {
		this.datas = datas;
	}
	
	/**
	 * ��ȡһ������
	 * @return
	 */
	public static Protocol newHeartBeat() {
		final Protocol p = new Protocol();
		p.type = TYPE_HEARTBEAT;
		p.length = 0;
		p.datas = new byte[0];
		return p;
	}

}
