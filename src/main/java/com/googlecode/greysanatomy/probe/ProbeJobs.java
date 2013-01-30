package com.googlecode.greysanatomy.probe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProbeJobs {

	private static final Logger logger = LoggerFactory.getLogger("greysanatomy");
	
	/**
	 * ����
	 */
	private static final AtomicInteger idseq = new AtomicInteger();

	/**
	 * ����
	 * @author vlinux
	 *
	 */
	private static class Job {
		private int id;
		private boolean isAlive;
		private JobListener listener;
	}
	
	private static final Map<Integer,Job> jobs = new ConcurrentHashMap<Integer, Job>();
	
	
	/**
	 * ע��������
	 * @param listener
	 */
	public static void register(int id, JobListener listener) {
		Job job = jobs.get(id);
		if( null != job ) {
			job.listener = listener;
			listener.create();
		}
	}
	
	/**
	 * ����һ��job
	 * @return
	 */
	public static int createJob() {
		final int id = idseq.incrementAndGet();
		Job job = new Job();
		job.id = id;
		job.isAlive = false;
		jobs.put(id, job);
		return id;
	}
	
	/**
	 * ����һ��job
	 * @param id
	 */
	public static void activeJob(int id) {
		Job job = jobs.get(id);
		if( null != job ) {
			job.isAlive = true;
		}
	}
	
	/**
	 * �ж�job�Ƿ񻹿��Լ�������
	 * @param id
	 * @return
	 */
	public static boolean isJobAlive(int id) {
		Job job = jobs.get(id);
		return null != job && job.isAlive;
	}
	
	/**
	 * ɱ��һ��job
	 * @param id
	 */
	public static void killJob(int id) {
		Job job = jobs.get(id);
		if( null != job ) {
			job.isAlive = false;
			try {
				job.listener.destroy();
			}catch(Throwable t) {
				logger.warn("destroy listener failed, jobId={}", id, t);
			}
		}
	}
	
	/**
	 * ���ش���jobId
	 * @return
	 */
	public static List<Integer> listAliveJobIds() {
		final List<Integer> jobIds = new ArrayList<Integer>();
		for(Job job : jobs.values()) {
			if( job.isAlive ) {
				jobIds.add(job.id);
			}
		}
		return jobIds;
	}
	
	/**
	 * ���ص�ǰ��̽��������б�
	 * @param id
	 * @return
	 */
	public static JobListener getJobListeners(int id) {
		if( jobs.containsKey(id) ) {
			return jobs.get(id).listener;
		} else {
			return null; 
		}
	}
	
	/**
	 * job�Ƿ�ʵ����ָ����listener
	 * @param id
	 * @param classListener
	 * @return
	 */
	public static boolean isListener(int id, Class<? extends JobListener> classListener) {
		
		final JobListener jobListener = getJobListeners(id);
		return null != jobListener 
				&& classListener.isAssignableFrom(jobListener.getClass());
		
	}
	
}
