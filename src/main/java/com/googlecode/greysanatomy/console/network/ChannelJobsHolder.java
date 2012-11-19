package com.googlecode.greysanatomy.console.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;

import com.googlecode.greysanatomy.probe.Probes;

/**
 * 服务端当前连接上job持有信息 
 * @author vlinux
 *
 */
public class ChannelJobsHolder {

	// 持有信息
	private final static Map<Channel, Set<Integer>> holder = new HashMap<Channel, Set<Integer>>();
	
	/**
	 * 注册一个job
	 * @param channel
	 * @param jobid
	 */
	public static synchronized void registJob(Channel channel, int jobid) {
		final Set<Integer> ids;
		if( !holder.containsKey(channel) ) {
			holder.put(channel, ids = new HashSet<Integer>());
		} else {
			ids = holder.get(channel);
		}//if
		ids.add(jobid);
		
	}
	
	/**
	 * 获取当前连接上的所有任务
	 * @param channel
	 * @return
	 */
	public static synchronized Set<Integer> getJobs(Channel channel) {
		if( holder.containsKey(channel) ) {
			return holder.get(channel);
		}
		return new HashSet<Integer>();
	}
	
	/**
	 * 注销一批任务
	 * @param channel
	 * @param jobids
	 */
	public static synchronized void unRegistJob(Channel channel, Set<Integer> jobids) {
		if( holder.containsKey(channel) ) {
			final Iterator<Integer> it = holder.get(channel).iterator();
			while( it.hasNext() ) {
				int id = it.next();
				if( jobids.contains(id) ) {
					Probes.killJob(id);
					it.remove();
				}
			}
		}
	}
	
	/**
	 * 注销一个连接上所有任务
	 * @param channel
	 */
	public static synchronized void unRegistJob(Channel channel) {
		if( holder.containsKey(channel) ) {
			final Iterator<Integer> it = holder.get(channel).iterator();
			while( it.hasNext() ) {
				int id = it.next();
				Probes.killJob(id);
				it.remove();
			}
			holder.remove(channel);
		}
	}
	
}
