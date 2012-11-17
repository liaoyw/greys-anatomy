package com.googlecode.greysanatomy.cmd.probe;

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

import com.googlecode.greysanatomy.cmd.RespCmd;
import com.googlecode.greysanatomy.cmd.RespCmd.RespCmdSender;
import com.googlecode.greysanatomy.cmd.annotation.Cmd;
import com.googlecode.greysanatomy.cmd.annotation.CmdArg;
import com.googlecode.greysanatomy.probe.Probe;
import com.googlecode.greysanatomy.probe.ProbeListener;
import com.googlecode.greysanatomy.probe.ProbeListenerAdapter;

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
@Cmd(name="monitor")
public class MonitorProbeCmd extends ProbeCmd {

	private static final long serialVersionUID = -5735449087616204781L;

//	/*
//	 * 命令参数：监听类正则表达式
//	 */
//	@CmdArg(name="enter-class", nullable=false)
//	private String enterClzRegex;
//	
//	/*
//	 * 命令参数：监听方法正则表达式
//	 */
//	@CmdArg(name="enter-method", nullable=false)
//	private String enterMthRegex;
	
	/*
	 * 命令参数：监听周期
	 */
	@CmdArg(name="cy")
	private int cycle = 120;

	/*
	 * 输出定时任务
	 */
	private Timer timer;
	
	
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
			Key okey = (Key)obj;
			return clazz.equals(okey.clazz) && method.equals(okey.method);
		}
		
	}
	private static class Data {
		private int total;
		private int success;
		private int failed;
		private long cost;
	}
	
	/*
	 * 监控数据
	 */
	private ConcurrentHashMap<Key, AtomicReference<Data>> monitorDatas = new ConcurrentHashMap<Key, AtomicReference<Data>>();
	
	@Override
	public ProbeListener getProbeListener(final RespCmdSender sender) {
		return new ProbeListenerAdapter(){

			private final ThreadLocal<Long> beginTimestamp = new ThreadLocal<Long>();
//			private final ThreadLocal<Boolean> isEnter = new ThreadLocal<Boolean>();
			
			
			@Override
			public void onBefore(Probe p) {
				
//				if( p.getTargetClass().getName().matches(enterClzRegex)
//						&& p.getTargetMethod().getName().matches(enterMthRegex)) {
//					beginTimestamp.set(System.currentTimeMillis());
//					isEnter.set(true);
//				}
				
				beginTimestamp.set(System.currentTimeMillis());
			}

			@Override
			public void onFinish(Probe p) {
//				if( !isEnter.get() ) {
//					return;
//				}
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
						
						sender.send(new RespCmd(MonitorProbeCmd.this.getId(), monitorSB.toString()));
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
			
			
			
		};
	}

}
