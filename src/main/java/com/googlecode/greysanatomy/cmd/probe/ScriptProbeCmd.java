package com.googlecode.greysanatomy.cmd.probe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.probe.Probe;
import com.googlecode.greysanatomy.probe.ProbeListener;
import com.googlecode.greysanatomy.probe.ProbeListenerAdapter;

@Cmd(name="script")
public class ScriptProbeCmd extends ProbeCmd {

	private static final long serialVersionUID = 3287190038048017909L;
	
	@CmdArg(name="sf", nullable=true)
	private String scriptFilename;

	public static class TLS {
		
		private final ThreadLocal<Map<String,Object>> tls = new ThreadLocal<Map<String,Object>>();
		
		public TLS() {
			tls.set(new HashMap<String,Object>());
		}
		
		public void put(String name, Object value) {
			tls.get().put(name,value);
		}
		
		public Object get(String name) {
			return tls.get().get(name);
		}
		
	}
	
	public static class Output {
		
		private final RespCmdSender sender;
		private final ProbeCmd probeCmd;
		
		public Output(ProbeCmd probeCmd, RespCmdSender sender) {
			this.sender = sender;
			this.probeCmd = probeCmd;
		}
		
		public void println(String msg) {
			sender.send(new RespCmd(probeCmd.getId(), msg));
		}
		
	}
	
	public static interface ScriptListener {
		
		void before(Probe p, Output output, TLS tls);
		void success(Probe p, Output output, TLS tls);
		void exception(Probe p, Output output, TLS tls);
		void finished(Probe p, Output output, TLS tls);
		
	}
	
	@Override
	public ProbeListener getProbeListener(final RespCmdSender sender) {
		
		if( null == scriptFilename
				|| scriptFilename.isEmpty()) {
			sender.send(new RespCmd(getId(), "script file not exist."));
			return null;
		}
		
		final File scriptFile = new File(scriptFilename);
		if( !scriptFile.isFile()
				|| !scriptFile.exists()
				|| !scriptFile.canRead()) {
			sender.send(new RespCmd(getId(), "script file not exist."));
			return null;
		}
		
		final TLS tls = new TLS();
		final Output output = new Output(this, sender);
		final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByExtension("js");
		final Invocable invoke = (Invocable) jsEngine;
		final ScriptListener scriptListener;
		try {
			jsEngine.eval("var $f=com.googlecode.greysanatomy.util.ClassUtils.getField;");
			jsEngine.eval(new FileReader(scriptFile));
			scriptListener = invoke.getInterface(ScriptListener.class);
		} catch (FileNotFoundException e) {
			sender.send(new RespCmd(getId(), "script file not exist."+e.getMessage(), e));
			return null;
		} catch (ScriptException e) {
			sender.send(new RespCmd(getId(), "script execute failed."+e.getMessage(), e));
			return null;
		}
		
		return new ProbeListenerAdapter(){

			@Override
			public void onBefore(final Probe p) {
				
				try {
					scriptListener.before(p, output, tls);
				}catch(Throwable t) {
					// ignore
				}
				
			}

			@Override
			public void onSuccess(final Probe p) {
				
				try {
					scriptListener.success(p, output, tls);
				}catch(Throwable t) {
					// ignore
				}
				
			}

			@Override
			public void onException(final Probe p) {
				
				try {
					scriptListener.exception(p, output, tls);
				}catch(Throwable t) {
					// ignore
				}
				
			}

			@Override
			public void onFinish(final Probe p) {
				
				try {
					scriptListener.finished(p, output, tls);
				}catch(Throwable t) {
					// ignore
				}
				
			}
			
			
			
		};
	}

}
