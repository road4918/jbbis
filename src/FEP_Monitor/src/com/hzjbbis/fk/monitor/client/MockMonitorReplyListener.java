package com.hzjbbis.fk.monitor.client;

import org.apache.log4j.Logger;

public class MockMonitorReplyListener implements IMonitorReplyListener {
	private static final Logger log = Logger.getLogger(MockMonitorReplyListener.class);

	public void onClose() {
		log.debug("��ط������ӹرա�");
	}

	public void onConnect() {
		log.debug("��ط������ӳɹ���");
	}

	public void onEventHookProfile(String profile) {
		log.info(profile);
	}

	public void onGetFile() {
		log.info("�����ļ��ɹ�");
	}

	public void onListConfig(String result) {
		log.info(result);
	}

	public void onListLog(String result) {
		log.info(result);
	}

	public void onMultiSysProfile(String profile) {
		log.info(profile);
	}

	public void onModuleProfile(String profile) {
		log.info(profile);
	}

	public void onPutFile() {
		log.info("�ϴ��ļ��ɹ�");
	}

	public void onReplyFailed(String reason) {
		log.info(reason);
	}

	public void onReplyOK() {
		log.info("�ɹ�");
	}

	public void onSystemProfile(String profile) {
		log.info(profile);
	}

	public void onRtuMessageInd(String ind) {
		log.info(ind);
	}

}
