/**
 * �¼�����ӿ�
 */
package com.hzjbbis.fk.common.spi;

/**
 * @author bao
 *
 */
public interface IEventPump {
	/**
	 * �첽��ʽ���¼����뵽�¼����С����¼������߳̽����¼��ַ��������¼����ӣ����̷߳�ʽ�����д���
	 * @param e
	 */
	void post(IEvent e);
}
