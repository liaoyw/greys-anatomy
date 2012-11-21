package com.googlecode.greysanatomy.console.command;

import static com.googlecode.greysanatomy.agent.GreysAnatomyClassFileTransformer.transform;
import static com.googlecode.greysanatomy.console.network.ChannelJobsHolder.registJob;
import static com.googlecode.greysanatomy.probe.ProbeJobs.activeJob;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.greysanatomy.agent.GreysAnatomyClassFileTransformer.TransformResult;
import com.googlecode.greysanatomy.console.command.annotation.Arg;
import com.googlecode.greysanatomy.console.command.annotation.Cmd;
import com.googlecode.greysanatomy.probe.Probe;
import com.googlecode.greysanatomy.probe.ProbeListenerAdapter;
import com.googlecode.greysanatomy.util.GaStringUtils;

/**
 * javascript������ǿ
 * 
 * @author vlinux
 * 
 */
@Cmd("javascript")
public class JavaScriptCommand extends Command {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	@Arg(name="class")
	private String classRegex;
	
	@Arg(name="method")
	private String methodRegex;
	
	@Arg(name="file")
	private File scriptFile;

	
	/**
	 * TLS = ThreadLocals
	 * @author vlinux
	 *
	 */
	public static class TLS {

		private final ThreadLocal<Map<String, Object>> tls = new ThreadLocal<Map<String, Object>>();

		public TLS() {
			tls.set(new HashMap<String, Object>());
		}

		public void put(String name, Object value) {
			tls.get().put(name, value);
		}

		public Object get(String name) {
			return tls.get().get(name);
		}

	}

	/**
	 * ���ű�ʹ�õ�output�����������Ϣ��ga-console-client
	 * @author vlinux
	 *
	 */
	public static class Output {

		private final Sender sender;

		public Output(Sender sender) {
			this.sender = sender;
		}

		public void println(String msg) {
			sender.send(false, msg);
		}

	}

	/**
	 * �ű�ʵ�ֽӿ�
	 * @author vlinux
	 *
	 */
	public static interface ScriptListener {

		void before(Probe p, Output output, TLS tls);
		void success(Probe p, Output output, TLS tls);
		void exception(Probe p, Output output, TLS tls);
		void finished(Probe p, Output output, TLS tls);
		void create(Output output, TLS tls);
		void destroy(Output output, TLS tls);

	}

	@Override
	public Action getAction() {
		return new Action(){

			@Override
			public void action(Info info, final Sender sender) throws Throwable {
				
				if( !scriptFile.isFile()
						|| !scriptFile.exists()
						|| !scriptFile.canRead()) {
					sender.send(true, "script file not exist.");
					return;
				}
				
				final TLS tls = new TLS();
				final Output output = new Output(sender);
				final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByExtension("js");
				final Invocable invoke = (Invocable) jsEngine;
				final ScriptListener scriptListener;
				try {
					jsEngine.eval("var $field=com.googlecode.greysanatomy.util.GaReflectUtils.getFieldValueByFieldName;");
					jsEngine.eval("var $jstack=com.googlecode.greysanatomy.util.GaReflectUtils.jstack;");
					jsEngine.eval(new FileReader(scriptFile));
					scriptListener = invoke.getInterface(ScriptListener.class);
				} catch (FileNotFoundException e) {
					final String msg = "script file not exist.";
					logger.warn(msg, e);
					sender.send(true, msg);
					return;
				} catch (ScriptException e) {
					final String msg = "script execute failed."+e.getMessage();
					logger.warn(msg, e);
					sender.send(true, msg);
					return;
				}
				
				final Instrumentation inst = info.getInst();
				final TransformResult result = transform(inst, classRegex, methodRegex, new ProbeListenerAdapter() {
					
					@Override
					public void onBefore(final Probe p) {
						try {scriptListener.before(p, output, tls);}catch(Throwable t) {}
					}

					@Override
					public void onSuccess(final Probe p) {
						try {scriptListener.success(p, output, tls);}catch(Throwable t) {}
					}

					@Override
					public void onException(final Probe p) {
						try {scriptListener.exception(p, output, tls);}catch(Throwable t) {}
					}

					@Override
					public void onFinish(final Probe p) {
						try {scriptListener.finished(p, output, tls);}catch(Throwable t) {}
					}

					@Override
					public void create() {
						try {scriptListener.create(output, tls);}catch(Throwable t) {}
					}

					@Override
					public void destroy() {
						try {scriptListener.destroy(output, tls);}catch(Throwable t) {}
					}
					
				});

				// ע������
				registJob(info.getChannel(), result.getId());
				
				// ��������
				activeJob(result.getId());
				
				final StringBuilder message = new StringBuilder();
				message.append(GaStringUtils.LINE);
				message.append(String.format("done. probe:c-Cnt=%s,m-Cnt=%s\n", 
						result.getModifiedClasses().size(),
						result.getModifiedBehaviors().size()));
				sender.send(false, message.toString());
			}
			
		};
	}

}
