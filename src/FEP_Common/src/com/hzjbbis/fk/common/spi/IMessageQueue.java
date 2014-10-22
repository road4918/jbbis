/**
 * 通信前置机、网关等，都需要一个消息队列来实现消息的上下行。
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
