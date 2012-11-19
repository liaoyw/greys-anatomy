package com.googlecode.greysanatomy.console.command;

import com.googlecode.greysanatomy.console.command.annotation.Arg;
import com.googlecode.greysanatomy.console.command.annotation.Cmd;

/**
 * ªÿœ‘√¸¡Ó
 * @author vlinux
 *
 */
@Cmd("echo")
public class EchoCommand extends Command {

	@Arg(name="words")
	private String words;

	@Override
	public Action getAction() {
		return new Action(){

			@Override
			public void action(Info info, Sender sender) {
				sender.send(true, words);
			}
			
		};
	}
	
}
