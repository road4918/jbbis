package com.hzjbbis.fk.sockclient;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.message.IMessage;

public class DumyJSocketListener implements JSocketListener {
	private static final Logger log = Logger.getLogger(DumyJSocketListener.class);
	
	public void onClose(JSocket client) {
		log.debug("socket client���ӹر�:"+client.getHostIp()+"@"+client.getHostPort());
	}

	public void onConnected(JSocket client) {
		log.debug("socket client���ӳɹ�:"+client.getHostIp()+"@"+client.getHostPort());
	}

	public void onReceive(JSocket client,IMessage msg) {
		log.debug("�յ���Ϣ:"+msg);
	}

	public void onSend(JSocket client,IMessage msg) {
		log.debug("���ͳɹ�:"+msg);
	}

}
