/**
 * ͨ��ǰ�û������صȣ�����Ҫһ����Ϣ������ʵ����Ϣ�������С�
 */
package com.hzjbbis.fk.common.spi;

import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 *
 */
public interface IMessageQueue {

	boolean sendMessage(IMessage msg);
	IMessage take()throws InterruptedException;
	IMessage poll();
	void offer(IMessage msg);
	int size();
}
