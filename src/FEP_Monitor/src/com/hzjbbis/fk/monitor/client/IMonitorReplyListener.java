package com.hzjbbis.fk.monitor.client;

public interface IMonitorReplyListener {
	void onSystemProfile(String profile);
	void onModuleProfile(String profile);
	void onEventHookProfile(String profile);
	void onMultiSysProfile(String profile);
	
	void onListLog(String result);
	void onListConfig(String result);
	
	void onGetFile();		//�ļ����سɹ�
	void onPutFile();		//�ļ��ϴ��ɹ�
	
	void onReplyOK();			//�ɹ�Ӧ����ģ����������ֹͣ
	void onReplyFailed(String reason);		//ʧ��Ӧ��.
	
	//RTU�ա����¼�����
	void onRtuMessageInd(String ind);
	
	//�����¼�
	void onConnect();
	void onClose();
}
