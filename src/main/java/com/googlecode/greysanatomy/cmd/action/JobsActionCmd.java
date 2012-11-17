package com.googlecode.greysanatomy.cmd.action;

import java.util.List;

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.probe.Probes;

/**
 * 列出当前正在执行的任务
 * @author vlinux
 *
 */
@Cmd(name="jobs")
public class JobsActionCmd extends ActionCmd {

	private static final long serialVersionUID = -1754774384841390724L;

	@Override
	public void doAction(RespCmdSender sender) throws Throwable {
		List<Integer> jobIds = Probes.listAliveJobIds();
		StringBuilder msgSB = new StringBuilder();
		if( jobIds.isEmpty() ) {
			msgSB.append("no jobs.");
		} else {
			for( Integer jobId : jobIds ) {
				msgSB.append(jobId).append("\n");
			}
		}
		sender.send(new RespCmd(this.getId(), msgSB.toString()));
	}

}
