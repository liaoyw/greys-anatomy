package com.googlecode.greysanatomy.console.command;

import static com.googlecode.greysanatomy.agent.GreysAnatomyClassFileTransformer.transform;
import static com.googlecode.greysanatomy.console.network.ChannelJobsHolder.registJob;
import static com.googlecode.greysanatomy.probe.ProbeJobs.activeJob;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.googlecode.greysanatomy.agent.GreysAnatomyClassFileTransformer.TransformResult;
import com.googlecode.greysanatomy.console.command.annotation.Arg;
import com.googlecode.greysanatomy.console.command.annotation.Cmd;
import com.googlecode.greysanatomy.probe.Probe;
import com.googlecode.greysanatomy.probe.ProbeListenerAdapter;
import com.googlecode.greysanatomy.util.GaStringUtils;

/**
 * 监控请求命令<br/>
 * 输出的内容格式为:<br/>
 * <style type="text/css">
 *   table, th, td {
 *   	border:1px solid #cccccc;
 *   	border-collapse:collapse;
 *   }
 * </style>
 * <table>
 *   <tr>
 *     <th>时间戳</th>
 *     <th>统计周期(s)</th>
 *     <th>类全路径</th>
 *     <th>方法名</th>
 *     <th>调用总次数</th>
 *     <th>成功次数</th>
 *     <th>失败次数</th>
 *     <th>平均耗时(ms)</th>
 *     <th>失败率</th>
 *   </tr>
 *   <tr>
 *     <td>2012-11-07 05:00:01</td>
 *     <td>120</td>
 *     <td>com.taobao.item.ItemQueryServiceImpl</td>
 *     <td>queryItemForDetail</td>
 *     <td>1500</td>
 *     <td>1000</td>
 *     <td>500</td>
 *     <td>15</td>
 *     <td>30%</td>
 *   </tr>
 *   <tr>
 *     <td>2012-11-07 05:00:01</td>
 *     <td>120</td>
 *     <td>com.taobao.item.ItemQueryServiceImpl</td>
 *     <td>queryItemById</td>
 *     <td>900</td>
 *     <td>900</td>
 *     <td>0</td>
 *     <td>7</td>
 *     <td>0%</td>
 *   </tr>
 * </table>
 * @author vlinux
 *
 */
@Cmd("monitor")
public class MonitorCommand extends Command {

	@Arg(name="class")
	private String classRegex;
	
	@Arg(name="method")
	private String methodRegex;
	
	@Arg(name="cycle")
	private int cycle = 120;
	
	/*
	 * 输出定时任务
	 */
	private Timer timer;
	
	/*
	 * 监控数据
	 */
	private ConcurrentHashMap<Key, AtomicReference<Data>> monitorDatas = new ConcurrentHashMap<Key, AtomicReference<Data>>();
	
	/**
	 * 数据监控用的Key
	 * @author vlinux
	 *
	 */
	private static class Key {
		private final Class<?> clazz;
		private final Method method;
		private Key(Class<?> clazz, Method method) {
			this.clazz = clazz;
			this.method = method;
		}
		@Override
		public int hashCode() {
			return clazz.hashCode() + method.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if( null == obj
					|| !(obj instanceof Key) ) {
				return false;
			}
			Key okey = (Key)obj;
			return clazz.equals(okey.clazz) && method.equals(okey.method);
		}
		
	}
	
	/**
	 * 数据监控用的value
	 * @author vlinux
	 *
	 */
	private static class Data {
		private int total;
		private int success;
		private int failed;
		private long cost;
	}
	
	@Override
	public Action getAction() {
		return new Action(){

			@Override
			public void action(Info info, final Sender sender) throws Throwable {
				
				final Instrumentation inst = info.getInst();
				final TransformResult result = transform(inst, classRegex, methodRegex, new ProbeListenerAdapter() {
					
					private final ThreadLocal<Long> beginTimestamp = new ThreadLocal<Long>();
					
					@Override
					public void onBefore(Probe p) {
						beginTimestamp.set(System.currentTimeMillis());
					}

					@Override
					public void onFinish(Probe p) {
						final long cost = System.currentTimeMillis() - beginTimestamp.get();
						final Key key = new Key(p.getTargetClass(),p.getTargetMethod());
						
						while(true) {
							AtomicReference<Data> value = monitorDatas.get(key);
							if( null == value ) {
								monitorDatas.putIfAbsent(key, new AtomicReference<Data>(new Data()));
								continue;
							}
							
							while(true) {
								Data oData = value.get();
								Data nData = new Data();
								nData.cost = oData.cost + cost;
								if( p.isThrowException() ) {
									nData.failed = oData.failed + 1;
								}
								if( p.isReturn() ) {
									nData.success = oData.success + 1;
								}
								nData.total = oData.total + 1;
								if(value.compareAndSet(oData, nData)) {
									break;
								}
							}
							
							break;
						}
					}

					@Override
					public void create() {
						timer = new Timer();
						timer.scheduleAtFixedRate(new TimerTask(){

							@Override
							public void run() {
								final String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
								final StringBuilder monitorSB = new StringBuilder();
								final Iterator<Map.Entry<Key, AtomicReference<Data>>> it = monitorDatas.entrySet().iterator();
								while( it.hasNext() ) {
									final Map.Entry<Key, AtomicReference<Data>> entry = it.next();
									final AtomicReference<Data> value = entry.getValue();
									final Data data = value.get();
									monitorSB.append(timestamp).append("\t");
									monitorSB.append(entry.getKey().clazz.getName()).append("\t");
									monitorSB.append(entry.getKey().method.getName()).append("\t");
									monitorSB.append(data.total).append("\t");
									monitorSB.append(data.success).append("\t");
									monitorSB.append(data.failed).append("\t");
									
									final DecimalFormat df = new DecimalFormat("0.00");
									monitorSB.append(df.format(div(data.cost, data.total))).append("\t");
									monitorSB.append(df.format(100.0d*div(data.failed,data.total))).append("%");
									monitorSB.append("\n");
									while(!value.compareAndSet(data, new Data()));
								}//while
								
								sender.send(false, monitorSB.toString());
							}
							
						}, 0, cycle*1000);
					}

					/**
					 * 绕过0的除法
					 * @param a
					 * @param b
					 * @return
					 */
					private double div(double a, double b) {
						if( b == 0 ) {
							return 0;
						}
						return a/b;
					}
					
					@Override
					public void destroy() {
						if( null != timer ) {
							timer.cancel();
						}
					}
					
				});

				// 注册任务
				registJob(info.getChannel(), result.getId());
				
				// 激活任务
				activeJob(result.getId());
				
				final StringBuilder message = new StringBuilder();
				message.append(GaStringUtils.LINE);
				message.append(String.format("done. probe:c-Cnt=%s,m-Cnt=%s\n", 
						result.getModifiedClasses().size(),
						result.getModifiedMethods().size()));
				sender.send(false, message.toString());
			}
			
		};
	}

}
