/**
 * 收到简单类型的消息对象事件
 */
package com.hzjbbis.fk.sockserver.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.sockserver.AsyncSocketClient;
import com.hzjbbis.fk.sockserver.message.SimpleMessage;

/**
 * @author bao
 *
 */
public class RecvSimpleMessageEvent implements IEvent {
	private EventType type = EventType.MSG_SIMPLE_RECV;
	private SimpleMessage message;
	private AsyncSocketClient client;
	
	public RecvSimpleMessageEvent(IMessage m){
		message = (SimpleMessage)m;
		client = (AsyncSocketClient)m.getSource();
	}
	
	public Object getSource() {
		return client.getServer();
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public void setType(EventType type) {
	}

	public SimpleMessage getMessage() {
		return message;
	}

	public void setMessage(SimpleMessage message) {
		this.message = message;
	}

	public AsyncSocketClient getClient() {
		return client;
	}

	public void setClient(AsyncSocketClient client) {
		this.client = client;
	}

}
