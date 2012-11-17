package com.googlecode.greysanatomy.cmd.action;

import com.googlecode.greysanatomy.cmd.BaseCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;

/**
 * ��������
 * @author vlinux
 *
 */
public abstract class ActionCmd extends BaseCmd {

	private static final long serialVersionUID = -8637283872157619991L;

	/**
	 * ִ�ж���
	 * @param sender
	 * @throws Throwable
	 */
	abstract public void doAction(RespCmdSender sender) throws Throwable;
	
}
