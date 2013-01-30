package com.googlecode.greysanatomy.console.network;

import java.rmi.Remote;

import com.googlecode.greysanatomy.console.network.coder.RespResult;
import com.googlecode.greysanatomy.console.network.coder.req.ReqCmd;
import com.googlecode.greysanatomy.console.network.coder.req.ReqGetResult;
import com.googlecode.greysanatomy.console.network.coder.req.ReqHeart;
import com.googlecode.greysanatomy.console.network.coder.req.ReqKillJob;

/**
 * ����̨�����interface
 * @author chengtongda
 *
 */
public interface ConsoleServerService extends Remote{

	/**
	 * ��������
	 * @param cmd
	 * @return
	 */
	public RespResult postCmd(ReqCmd cmd) throws Exception;
	
	/**
	 * ע�����
	 * @return
	 */
	public long register() throws Exception;
	
	/**
	 * ��ȡ����ִ�н��
	 * @param cmd
	 * @return
	 */
	public RespResult getCmdExecuteResult(ReqGetResult req) throws Exception;
	
	/**
	 * ɱ������
	 * @param cmd
	 */
	public void killJob(ReqKillJob req) throws Exception;
	
	/**
	 * session����
	 * @param cmd
	 */
	public boolean sessionHeartBeat(ReqHeart req) throws Exception;
	
}
