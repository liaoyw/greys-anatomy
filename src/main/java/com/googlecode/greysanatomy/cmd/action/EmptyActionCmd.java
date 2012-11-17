package com.googlecode.greysanatomy.cmd.action;

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;

/**
 * ¿Õ¶¯×÷ÃüÁî
 * @author vlinux
 *
 */
public class EmptyActionCmd extends ActionCmd {

	private static final long serialVersionUID = 4930063363263213855L;

	@Override
	public void doAction(RespCmdSender sender) throws Throwable {
		sender.send(new RespCmd(this.getId(), null));
	}

}
