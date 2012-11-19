package com.googlecode.greysanatomy.console.command;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import com.googlecode.greysanatomy.console.command.annotation.Arg;
import com.googlecode.greysanatomy.console.command.annotation.ArgVerifier;
import com.googlecode.greysanatomy.console.command.annotation.Cmd;
import com.googlecode.greysanatomy.util.GaReflectUtils;

public class Commands {

	private final Map<String, Class<?>> commands = new HashMap<String,Class<?>>();
	
	private Commands() {
		
		for(Class<?> clazz : GaReflectUtils.getClasses("com.googlecode.greysanatomy.console.command")) {
			
			if( !Command.class.isAssignableFrom(clazz) 
					|| Modifier.isAbstract(clazz.getModifiers())
					|| !clazz.isAnnotationPresent(Cmd.class)) {
				continue;
			}
			
			final Cmd cmd = clazz.getAnnotation(Cmd.class);
			commands.put(cmd.value(), clazz);
			
		}
		
	}
	
	/**
	 * ��ȡ���б�ע��arg��field
	 * @param clazz
	 * @return
	 */
	private static Set<Field> getArgFields(Class<?> clazz) {
		final Set<Field> fields = new HashSet<Field>();
		for( Field field : GaReflectUtils.getFileds(clazz) ) {
			if( !field.isAnnotationPresent(Arg.class) ) {
				continue;
			}
			fields.add(field);
		}
		return fields;
	}
	
	private static OptionParser getOptionParser(Class<?> clazz) {
		final OptionParser parser = new OptionParser();
		for( Field field : getArgFields(clazz) ) {
			final Arg arg = field.getAnnotation(Arg.class);
			parser.accepts(arg.name()).withRequiredArg().ofType(field.getType()).required();
		}
		return parser;
	}
	
	/**
	 * У��
	 * @param arg
	 * @param value
	 */
	private static void verifyArg(Arg arg, Object obj) {

		final String value = null == obj ? "" : obj.toString() ;
		final ArgVerifier[] verifies = arg.verify();
		if( null == value
				|| value.isEmpty()) {
			if( arg.isRequired() ) {
				throw new IllegalArgumentException(String.format("arg:%s is required, but it's empty now!", arg.name()));
			}
		} else {
			if( null == verifies ) {
				return;
			}
			for( ArgVerifier av : verifies ) {
				if( !value.matches(av.regex()) ) {
					throw new IllegalArgumentException(String.format("arg:%s is illegal. because %s", arg.name(), av.description()));
				}
			}
		}
		
	}
	
	/**
	 * �½�һ������
	 * @param line
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public Command newCommand(String line) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		final String[] strs = line.split("\\s+");
		final String cmdName = strs[0];
		final Class<?> clazz = getInstance().commands.get(cmdName);
		if( null == clazz ) {
			return null;
		}
		final Command command = (Command) clazz.newInstance();
		final OptionSet opt = getOptionParser(clazz).parse(strs);
		
		for( Field field : getArgFields(clazz) ) {
			final Arg arg = field.getAnnotation(Arg.class);
			if( opt.has(arg.name()) ) {
				final Object value = opt.valueOf(arg.name());
				verifyArg(arg, value);
				GaReflectUtils.set(field, value, command);
			}
		}//for
		
		return command;
		
	}

	/**
	 * �г���������
	 * @return
	 */
	public Map<String, Class<?>> listCommands() {
		return new HashMap<String, Class<?>>(commands);
	}
	
	
	/**
	 * ��ȡ���е������в���
	 * @return
	 */
	private Collection<Completer> getCommandCompleters() {
		final Collection<Completer> completers = new ArrayList<Completer>();
		
		for( Map.Entry<String, Class<?>> entry : Commands.getInstance().listCommands().entrySet() ) {
			ArgumentCompleter argCompleter = new ArgumentCompleter();
			completers.add(argCompleter);
			argCompleter.getCompleters().add(new StringsCompleter(entry.getKey()));
			final Set<String> fields = new HashSet<String>();
			for( Field field : GaReflectUtils.getFileds(entry.getValue()) ) {
				if( field.isAnnotationPresent(Arg.class) ) {
					Arg arg = field.getAnnotation(Arg.class);
					fields.add("-"+arg.name());
				}
			}//for
			argCompleter.getCompleters().add(new StringsCompleter(fields));
		}
		return completers;
	}
	
	/**
	 * ע����ʾ��Ϣ
	 * @param console
	 */
	public void registCompleter(ConsoleReader console) {
		console.addCompleter(new AggregateCompleter(getCommandCompleters()));
	}
	
	private static final Commands instance = new Commands();
	
	/**
	 * ��ȡ����
	 * @return
	 */
	public static synchronized Commands getInstance() {
		return instance;
	}
	
}