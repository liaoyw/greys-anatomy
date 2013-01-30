package com.googlecode.greysanatomy.console.command;

import static com.googlecode.greysanatomy.agent.GreysAnatomyClassFileTransformer.transform;
import static com.googlecode.greysanatomy.console.network.ChannelJobsHolder.registJob;
import static com.googlecode.greysanatomy.probe.ProbeJobs.activeJob;

import java.lang.instrument.Instrumentation;

import com.googlecode.greysanatomy.agent.GreysAnatomyClassFileTransformer.TransformResult;
import com.googlecode.greysanatomy.console.command.annotation.Arg;
import com.googlecode.greysanatomy.console.command.annotation.Cmd;
import com.googlecode.greysanatomy.probe.Advice;
import com.googlecode.greysanatomy.probe.AdviceListenerAdapter;
import com.googlecode.greysanatomy.util.GaStringUtils;
import com.googlecode.greysanatomy.util.ProfilerUtils;

@Cmd("profiler")
public class ProfilerCommand extends Command {

	@Arg(name="class")
	private String classRegex;
	
	@Arg(name="method")
	private String methodRegex;
	
	@Arg(name="probe-class")
	private String probeClassRegex;
	
	@Arg(name="probe-method")
	private String probeMethodRegex;
	
	@Arg(name="cost")
	private long cost;
	
	@Override
	public Action getAction() {
		return new Action(){

			private final ThreadLocal<Boolean> isEntered = new ThreadLocal<Boolean>();
			private final ThreadLocal<Integer> deep = new ThreadLocal<Integer>();
			private final ThreadLocal<Long> beginTimestamp = new ThreadLocal<Long>();
			
			@Override
			public void action(Info info, final Sender sender) throws Throwable {
				
				final Instrumentation inst = info.getInst();
				final TransformResult result = transform(inst, classRegex, methodRegex, new AdviceListenerAdapter() {
					
					@Override
					public void onBefore(Advice p) {
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
					public void onFinish(Advice p) {
						if( !isEntered.get() ) {
							return;
						}
						deep.set(deep.get()-1);
						ProfilerUtils.release();
						if( 0 == deep.get() ) {
							final long cost = System.currentTimeMillis() - beginTimestamp.get();
							final String dump = ProfilerUtils.dump();
							if( cost >= ProfilerCommand.this.cost ) {
								sender.send(false, dump);
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
					
					private boolean isEntered(Advice p) {
						return isEntered.get()
								|| (p.getTarget().getTargetClass().getName().matches(probeClassRegex) && p.getTarget().getTargetBehavior().getName().matches(probeMethodRegex));
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
