package com.googlecode.greysanatomy.console.network;

import static com.googlecode.greysanatomy.probe.ProbeJobs.killJob;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ����˵�ǰ������job������Ϣ 
 * @author vlinux
 *
 */
public class ChannelJobsHolder {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
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
		
		logger.info("regist job={} for channel={}", jobid, channel.getRemoteAddress());
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
		return Collections.emptySet();
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
					killJob(id);
					it.remove();
					logger.info("unRegist job={} for channel={}", id, channel.getRemoteAddress());
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
				killJob(id);
				it.remove();
				logger.info("unRegist job={} for channel={}", id, channel.getRemoteAddress());
			}
			holder.remove(channel);
		}
	}
	
}
