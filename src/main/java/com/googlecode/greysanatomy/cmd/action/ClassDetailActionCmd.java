package com.googlecode.greysanatomy.cmd.action;

import static java.lang.String.format;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.util.RefDetailUtils;

@Cmd(name = "detail-class")
public class ClassDetailActionCmd extends ActionCmd {

	private static final long serialVersionUID = 5352482967201077328L;

	@CmdArg(name = "c", nullable = false)
	private String classRegex;
	
	@CmdArg(name = "s", nullable = true)
	private String superClassRegex;

	private Set<Class<?>> searchForSuperClass(Class<?>[] allClass) {
		
		final Set<Class<?>> superClass = new HashSet<Class<?>>();
		
		if( null == superClassRegex
				|| superClassRegex.isEmpty()) {
			return superClass;
		}
		
		
		for( Class<?> clazz : allClass ) {
			if( clazz.getName().matches(superClassRegex) ) {
				superClass.add(clazz);
			}
		}
		
		return superClass;
		
	}
	
	private boolean isSuperClass(Set<Class<?>> superClasses, Class<?> clazz) {
		if( superClasses.isEmpty() ) {
			return false;
		}
		
		for( Class<?> superClass : superClasses ) {
			if( superClass.isAssignableFrom(clazz) ) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void doAction(RespCmdSender sender) throws Throwable {

		if (null == classRegex || classRegex.isEmpty()) {
			classRegex = ".*";
		}
		
		boolean isNeedSuperClass = true;
		if( null == superClassRegex
				|| superClassRegex.isEmpty()) {
			isNeedSuperClass = false;
		}

		final StringBuilder sendSB = new StringBuilder();
		final Instrumentation inst = getInst();
		int clzCnt = 0;

		final Class<?>[] allClass = inst.getAllLoadedClasses();
		
		Set<Class<?>> superClass = null;
		if( isNeedSuperClass ) {
			superClass = searchForSuperClass(allClass);
		}
		
		for (Class<?> clazz : allClass) {

			if (!clazz.getName().matches(classRegex)) {
				continue;
			}
			
			if( isNeedSuperClass && !isSuperClass(superClass, clazz)) {
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
