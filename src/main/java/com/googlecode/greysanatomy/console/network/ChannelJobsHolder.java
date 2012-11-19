package com.googlecode.greysanatomy.console.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;

import com.googlecode.greysanatomy.probe.Probes;

/**
 * ����˵�ǰ������job������Ϣ 
 * @author vlinux
 *
 */
public class ChannelJobsHolder {

	// ������Ϣ
	private final static Map<Channel, Set<Integer>> holder = new HashMap<Channel, Set<Integer>>();
	
	/**
	 * ע��һ��job
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
	 * ��ȡ��ǰ�����ϵ���������
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
	 * ע��һ������
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
	 * ע��һ����������������
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
