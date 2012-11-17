package com.googlecode.greysanatomy.cmd.probe;

import com.googlecode.greysanatomy.cmd.BaseCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.probe.ProbeListener;

/**
 * 探测器命令类
 * @author vlinux
 *
 */
public abstract class ProbeCmd extends BaseCmd {

	private static final long serialVersionUID = -2522667489911567386L;

	/*
	 * 命令参数：监听类正则表达式
	 */
	@CmdArg(name="class", nullable=false)
	private String perfClzRegex;
	
	/*
	 * 命令参数：监听方法正则表达式
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
	 * 获取侦听器
	 * @param sender
	 * @return 返回侦听器实现,不同的命令侦听器实现的方式也各自不同，所以这里做成抽象类由具体命令实现
	 */
	abstract public ProbeListener getProbeListener(RespCmdSender sender);
	
}
