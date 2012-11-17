package com.googlecode.greysanatomy.cmd.action;

import static java.lang.String.format;

import java.lang.instrument.Instrumentation;

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.util.RefDetailUtils;

@Cmd(name = "detail-class")
public class ClassDetailActionCmd extends ActionCmd {

	private static final long serialVersionUID = 5352482967201077328L;

	@CmdArg(name = "class", nullable = false)
	private String classRegex;

	@Override
	public void doAction(RespCmdSender sender) throws Throwable {

		if (null == classRegex || classRegex.isEmpty()) {
			classRegex = ".*";
		}

		final StringBuilder sendSB = new StringBuilder();
		final Instrumentation inst = getInst();
		int clzCnt = 0;

		for (Class<?> clazz : inst.getAllLoadedClasses()) {

			if (!clazz.getName().matches(classRegex)) {
				continue;
			}
			
			clzCnt++;
			
			sendSB.append(RefDetailUtils.detail(clazz)).append("\n");
			
		}//for
		
		sendSB.append("---------------------------------------------------------------\n");
		sendSB.append(format("done. detail-class result: match-class=%s;\n", clzCnt));
		
		sender.send(new RespCmd(this.getId(), sendSB.toString()));

	}

}
