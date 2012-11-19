package com.googlecode.greysanatomy.console.command;

import static java.lang.String.format;

import com.googlecode.greysanatomy.console.command.annotation.Arg;
import com.googlecode.greysanatomy.console.command.annotation.Cmd;
import com.googlecode.greysanatomy.util.GaDetailUtils;

/**
 * 展示类信息
 * @author vlinux
 *
 */
@Cmd("detail-class")
public class DetailClassCommand extends Command {

	@Arg(name="class",isRequired=true)
	private String classRegex;
	
	@Override
	public Action getAction() {
		return new Action(){

			@Override
			public void action(final Info info, final Sender sender) throws Throwable {
				
				final StringBuilder message = new StringBuilder();
				int clzCnt = 0;
				for (Class<?> clazz : info.getInst().getAllLoadedClasses()) {

					if (clazz.getName().matches(classRegex)) {
						message.append(GaDetailUtils.detail(clazz)).append("\n");
						clzCnt++;
					}
					
				}//for
				
				message.append("---------------------------------------------------------------\n");
				message.append(format("done. classes result: match-class=%s;\n", clzCnt));
				sender.send(true, message.toString());
			}
			
		};
	}
	
}
