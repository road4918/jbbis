/**
 * ����Socket���������󹫹��ӿڶ��壬�����첽TCP�������Լ�ͬ��UDP��������
 */
package com.hzjbbis.fk.common.spi.socket;

import com.hzjbbis.fk.common.spi.IModStatistics;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author Administrator
 *
 */
public interface ISocketServer extends IModStatistics,IModule{
	/**
	 * �����������˿�
	 * @return
	 */
	int getPort();
	
	/**
	 * Ϊ��֧���ն����ӵ�������ʵ�ʶ�ӦIP��ַ����Ҫ����IP:PORT��
	 * ��socket��������ȡ��ip��ַ����ӳ�䵽�����ĵ�ַ��
	 * @return
	 */
	String getServerAddress();
	
	/**
	 * ��������IO�����������ڴ����յ�������������Ҫ������������
	 * IOHandler�ܹ����յ��������н�������������Ϣ�����߰���Ϣ���������������ͳ�ȥ��
	 * @return
	 */
	IClientIO getIoHandler();
	
	/**
	 * ��������IO�����̳߳ش�С�������ն�TCP������������ÿ100���ն�����1��IO�̡߳�
	 * @return
	 */
	int getIoThreadSize();
	
	/**
	 * ���ͻ��˶Ͽ�����ʱ���ӷ�������ά�����б���ɾ����
	 * @param client
	 */
	void removeClient(IServerSideChannel client);
	
	int getClientSize();	//�������Ѿ����ӵ�socket client������
	/**
	 * ���ر��������������client���顣
	 * @return
	 */
	IServerSideChannel[] getClients();
	
	/**
	 * �������������Լ��ܹ��������Ϣ�����Ա��Լ���IOHandler���ж�д����
	 * @return
	 */
	IMessage createMessage();
	
	/**
	 * ������������ȱʡsocket���������ȡ�
	 * @return
	 */
	int getBufLength();
	

	/**
	 * ����ͨ���������ơ���ֹ���ɶ�ȡ���ݣ�����������Ӧ��
	 * @return
	 */
	int getMaxContinueRead();
	int getWriteFirstCount();
	
	/**
	 * �����������ͳ����Ϣ�Ĵ���
	 */
	void setLastReceiveTime(long lastRecv);
	void setLastSendTime(long lastSend);
	void incRecvMessage();
	void incSendMessage();
}
