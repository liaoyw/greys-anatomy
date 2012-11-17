package com.googlecode.greysanatomy.cmd.probe;

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.probe.Probe;
import com.googlecode.greysanatomy.probe.ProbeListener;
import com.googlecode.greysanatomy.probe.ProbeListenerAdapter;
import com.googlecode.greysanatomy.util.ProfilerUtils;

@Cmd(name="profile")
public class ProfilerProbeCmd extends ProbeCmd {

	private static final long serialVersionUID = -1795772179582599885L;

	@CmdArg(name="enter-class", nullable=false)
	private String enterClassRegex;
	
	@CmdArg(name="enter-method", nullable=false)
	private String enterMethodRegex;
	
	@CmdArg(name="cost-limit", nullable=false)
	private long costLimit;
	
	@Override
	public ProbeListener getProbeListener(final RespCmdSender sender) {
		return new ProbeListenerAdapter(){

			private final ThreadLocal<Boolean> isEntered = new ThreadLocal<Boolean>();
			private final ThreadLocal<Integer> deep = new ThreadLocal<Integer>();
			private final ThreadLocal<Long> beginTimestamp = new ThreadLocal<Long>();
			
			@Override
			public void onBefore(Probe p) {
				init();
				if( !isEntered(p) ) {
					return;
				}
				if( 0 == deep.get() ) {
					beginTimestamp.set(System.currentTimeMillis());
					isEntered.set(true);
					ProfilerUtils.start("");
				}
				ProfilerUtils.enter();
				deep.set(deep.get()+1);
			}

			@Override
			public void onFinish(Probe p) {
				if( !isEntered.get() ) {
					return;
				}
				deep.set(deep.get()-1);
				ProfilerUtils.release();
				if( 0 == deep.get() ) {
					final long cost = System.currentTimeMillis() - beginTimestamp.get();
					final String dump = ProfilerUtils.dump();
					if( cost >= costLimit ) {
						sender.send(new RespCmd(ProfilerProbeCmd.this.getId(), dump));
					}
					isEntered.set(false);
				}
			}
			
			private void init() {
				if( null == deep.get() ) {
					deep.set(0);
				}
				if( null == isEntered.get() ) {
					isEntered.set(false);
				}
			}
			
			private boolean isEntered(Probe p) {
				return isEntered.get()
						|| (p.getTargetClass().getName().matches(enterClassRegex) && p.getTargetMethod().getName().matches(enterMethodRegex));
			}
			
		};
	}
	
}
