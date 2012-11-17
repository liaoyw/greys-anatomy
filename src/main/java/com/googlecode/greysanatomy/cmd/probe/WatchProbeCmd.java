package com.googlecode.greysanatomy.cmd.probe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

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

@Cmd(name="w")
public class WatchProbeCmd extends ProbeCmd {

	private static final long serialVersionUID = 3287190038048017909L;
	
	@CmdArg(name="sfn", nullable=true)
	private String scriptFilename;

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
		
		void before(Probe p, Output output);
		void success(Probe p, Output output);
		void exception(Probe p, Output output);
		void finished(Probe p, Output output);
		
	}
	
	@Override
	public ProbeListener getProbeListener(final RespCmdSender sender) {
		
		if( null == scriptFilename
				|| scriptFilename.isEmpty()) {
			return null;
		}
		
		final File scriptFile = new File(scriptFilename);
		if( !scriptFile.isFile()
				|| !scriptFile.exists()
				|| !scriptFile.canRead()) {
			return null;
		}
		
		final Output output = new Output(this, sender);
		final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByExtension("js");
		final Invocable invoke = (Invocable) jsEngine;
		final ScriptListener scriptListener;
		try {
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
					scriptListener.before(p, output);
				}catch(Throwable t) {
					// ignore
				}
				
			}

			@Override
			public void onSuccess(final Probe p) {
				
				try {
					scriptListener.success(p, output);
				}catch(Throwable t) {
					// ignore
				}
				
			}

			@Override
			public void onException(final Probe p) {
				
				try {
					scriptListener.exception(p, output);
				}catch(Throwable t) {
					// ignore
				}
				
			}

			@Override
			public void onFinish(final Probe p) {
				
				try {
					scriptListener.finished(p, output);
				}catch(Throwable t) {
					// ignore
				}
				
			}
			
			
			
		};
	}

}
