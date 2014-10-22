/**
 * ͨ����·����ӿڣ�����
 * 1������TCP��UDP server�˵Ŀͻ�����ͨ����·��
 * 2��TCP��UDP client��ͨ����·��
 * 3������ͨ�ŵ���·��
 */
package com.hzjbbis.fk.common.spi.socket;

import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 * 2008-11-05 14:32
 */
public interface IChannel {
	void close();

	/**
	 * ͨ��client��������Ϣ�������첽����ͬ�����͡�
	 * @param msg
	 */
	boolean send(IMessage msg);
	
	/**
	 * ����첽tcp socket client���󣬷��ط��Ͷ��д�С��
	 * @return
	 */
	int sendQueueSize();
	void setMaxSendQueueSize(int maxSendQueueSize);
	
	/**
	 * �����첽TCP socket server����UDP socket server����;
	 * �����첽socket client���ӳأ��������ӳض��󣨱���ʵ��ISocketServer)
	 * @return
	 */
	ISocketServer getServer();
	
	/**
	 * ���ClientChannel���첽ͨ�ŷ�ʽ��һ��������ĳ��IOThread����
	 * @param threadObj
	 */
	void setIoThread(Object threadObj);

	String getPeerIp();
	int getPeerPort();
	/**
	 * ���� peerIp:peerPort�ַ���
	 * @return
	 */
	String getPeerAddr();
	
	/**
	 * ͨ����ͨ��ʱ����� ioTime��������ʱ�� readTime��
	 * @return
	 */
	long getLastIoTime();
	void setLastIoTime();	//�ѵ�ǰʱ������Ϊ����IO����ʱ��
	long getLastReadTime();
	void setLastReadTime(); //�ѵ�ǰʱ����Ϊ����Ϣ��ʱ�䡣
	
	/**
	 * ֧�ֿͻ���������������͵ı������������
	 * requestNum�ݼ���0������������ٷ��ͱ��ģ��ȵ��ͻ��������͡�
	 * @param reqNum
	 */
	void setRequestNum(int reqNum);
	int  getRequestNum();

}
