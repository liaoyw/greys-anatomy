package com.googlecode.greysanatomy.cmd.probe;

import com.googlecode.greysanatomy.cmd.BaseCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.probe.ProbeListener;

/**
 * ̽����������
 * @author vlinux
 *
 */
public abstract class ProbeCmd extends BaseCmd {

	private static final long serialVersionUID = -2522667489911567386L;

	/*
	 * ���������������������ʽ
	 */
	@CmdArg(name="class", nullable=false)
	private String perfClzRegex;
	
	/*
	 * �����������������������ʽ
	 */
	@CmdArg(name="method", nullable=false)
	private String perfMthRegex;
	
	public String getPerfClzRegex() {
		return perfClzRegex;
	}

	public String getPerfMthRegex() {
		return perfMthRegex;
	}
	
	/**
	 * ��ȡ������
	 * @param sender
	 * @return ����������ʵ��,��ͬ������������ʵ�ֵķ�ʽҲ���Բ�ͬ�������������ɳ������ɾ�������ʵ��
	 */
	abstract public ProbeListener getProbeListener(RespCmdSender sender);
	
}
