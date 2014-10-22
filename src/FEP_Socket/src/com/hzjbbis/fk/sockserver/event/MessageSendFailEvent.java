/**
 * ���ͻ���AsyncSocketClient���Ͷ������ˣ�����client����ʧ��ʱ���������¼���
 */
package com.hzjbbis.fk.sockserver.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bao
 *
 */
public class MessageSendFailEvent implements IEvent {
	private final EventType type = EventType.MSG_SEND_FAIL;
	private IChannel client;
	private IMessage message;
	
	public MessageSendFailEvent(IMessage msg,IChannel c){
		message = msg;
		client = c;
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}

	public IChannel getClient() {
		return client;
	}

	public IMessage getMessage() {
		return message;
	}

	public Object getSource() {
		return client.getServer();
	}

	public void setSource(Object src) {
	}

	public String toString(){
		StringBuffer sb = new StringBuffer(1024);
		sb.append("message send failed event. client=");
		sb.append(client).append(",server=").append(client.getServer().getPort());
		sb.append(",messge=").append(message);
		return sb.toString();
	}
}
