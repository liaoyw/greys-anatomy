package com.googlecode.greysanatomy.cmd.action;

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.probe.Probes;

@Cmd(name="kill")
public class KillJobActionCmd extends ActionCmd {

	private static final long serialVersionUID = 2822549602603819298L;

	@CmdArg(name="id")
	private int jobId = 0;
	
	@Override
	public void doAction(RespCmdSender sender) throws Throwable {
		
		if( jobId > 0 ) {
			if( !Probes.isJobAlive(jobId) ) {
				sender.send(new RespCmd(getId(), String.format("job(id=%s) was not alive.", jobId)));
			} else {
				Probes.killJob(jobId);
				sender.send(new RespCmd(getId(), String.format("job(id=%s) has been killed.", jobId)));
			}
		} else {
			for(Integer id : Probes.listAliveJobIds()) {
				Probes.killJob(id);
			}
			sender.send(new RespCmd(getId(), "all jobs has been killed."));
		}
		
		
	}

}
