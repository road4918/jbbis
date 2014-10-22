/**
 * �¼������ӡ�ȫ���¼�������������ض������¼��������Ӵ���
 */
package com.hzjbbis.fk.common.spi;


/**
 * @author bao
 *
 */
public interface IEventHook extends IEventHandler{
	/**
	 * ������ϢԴ��ֻ��source��Event�е�source��ͬ���¼��ű�ִ�С�
	 * @param source
	 */
	void setSource(Object source);
	/**
	 * �첽�¼����� ֪ͨ
	 * @param e
	 */
	void postEvent(IEvent e);
	
	/**
	 * ����EventHook�����Ա�����¼�����
	 */
	boolean start();
	
	/**
	 * ֹͣEventHook������¼�����
	 */
	void stop();
	
	/**
	 * �¼���������profile��Ϣ�����¼����д�С�ȡ�
	 * ��ʽ��<profile>
	 * 			<queue size="123"/>
	 * 			<threads size="4"/>
	 * 		</profile>
	 * @return
	 */
	String profile();
	
	/**
	 * �ϴ��¼�����ʱ�䡣
	 * @return
	 */
	long getLastEventTime();
	
	void setEventTrace(IEventTrace eTrace);
}
