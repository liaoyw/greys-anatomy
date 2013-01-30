package com.googlecode.greysanatomy.console.command;

import static java.lang.String.format;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.greysanatomy.console.command.annotation.Arg;
import com.googlecode.greysanatomy.console.command.annotation.Cmd;
import com.googlecode.greysanatomy.util.GaDetailUtils;
import com.googlecode.greysanatomy.util.GaStringUtils;

/**
 * չʾ����Ϣ
 * @author vlinux
 *
 */
@Cmd("search-class")
public class SearchClassCommand extends Command {

	@Arg(name="class")
	private String classRegex;
	
	@Arg(name="is-super", isRequired=false)
	private boolean isSuper = false;
	
	@Arg(name="is-detail", isRequired=false)
	private boolean isDetail = false;
	
	
	/**
	 * ��������������ʽ��
	 * @param inst
	 * @return
	 */
	private Set<Class<?>> searchClassByClassRegex(Instrumentation inst) {
		final Set<Class<?>> matchs = new HashSet<Class<?>>();
		for (Class<?> clazz : inst.getAllLoadedClasses()) {
			if (clazz.getName().matches(classRegex)) {
				matchs.add(clazz);
			}
		}//for
		return matchs;
	}
	
	/**
	 * ���ݸ���������
	 * @param inst
	 * @param supers
	 * @return
	 */
	private Set<Class<?>> searchClassBySupers(Instrumentation inst, Set<Class<?>> supers) {
		final Set<Class<?>> matchs = new HashSet<Class<?>>();
		for (Class<?> clazz : inst.getAllLoadedClasses()) {
			for( Class<?> superClass : supers ) {
				if( superClass.isAssignableFrom(clazz) ) {
					matchs.add(clazz);
					break;
				}
			}
		}//for
		return matchs;
	}
	
	
	@Override
	public Action getAction() {
		return new Action(){

			@Override
			public void action(final Info info, final Sender sender) throws Throwable {
				
				final StringBuilder message = new StringBuilder();
				final Set<Class<?>> matchs;
				if( isSuper ) {
					matchs = searchClassBySupers(info.getInst(), searchClassByClassRegex(info.getInst()));
				} else {
					matchs = searchClassByClassRegex(info.getInst());
				}
				
				for( Class<?> clazz : matchs ) {
					if( isDetail ) {
						message.append(GaDetailUtils.detail(clazz)).append("\n");
					} else {
						message.append(clazz.getName()).append("\n");
					}
				}
				
				message.append(GaStringUtils.LINE);
				message.append(format("done. classes result: match-class=%s;\n", matchs.size()));
				sender.send(true, message.toString());
			}
			
		};
	}
	
}
