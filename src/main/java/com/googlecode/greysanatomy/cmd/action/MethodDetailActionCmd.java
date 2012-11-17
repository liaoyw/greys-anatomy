package com.googlecode.greysanatomy.cmd.action;

import static java.lang.String.format;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.util.RefDetailUtils;

@Cmd(name="detail-method")
public class MethodDetailActionCmd extends ActionCmd {

	private static final long serialVersionUID = -2356735884270740021L;

	@CmdArg(name="class", nullable=false)
	private String classRegex;
	
	@CmdArg(name="method", nullable=false)
	private String methodRegex;
	
	@Override
	public void doAction(RespCmdSender sender) throws Throwable {
		
		if( null == classRegex
				|| classRegex.isEmpty()) {
			classRegex = ".*";
		}
		
		if( null == methodRegex
				|| methodRegex.isEmpty()) {
			methodRegex = ".*";
		}
		
		final StringBuilder sendSB = new StringBuilder();
		final Instrumentation inst = getInst();
		int clzCnt = 0;
		int mthCnt = 0;
		for(Class<?> clazz : inst.getAllLoadedClasses()) {
			
			if( !clazz.getName().matches(classRegex) ) {
				continue;
			}
			
			boolean hasMethod = false;
			for( Method method : clazz.getDeclaredMethods() ) {
				
				if( !method.getName().matches(methodRegex) ) {
					continue;
				}
				
				sendSB.append(RefDetailUtils.detail(method)).append("\n");
				
				mthCnt++;
				hasMethod = true;
				
			}//for
			
			if( hasMethod ) {
				clzCnt++;
			}
			
		}//for
		
		sendSB.append("---------------------------------------------------------------\n");
		sendSB.append(format("done. detail-method result: match-class=%s; match-method=%s\n", clzCnt, mthCnt));
		
		sender.send(new RespCmd(this.getId(), sendSB.toString()));
		
	}
}
