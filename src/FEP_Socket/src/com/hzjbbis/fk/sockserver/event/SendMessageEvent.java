/**
 * 通用的发送消息事件定义
 */
package com.hzjbbis.fk.sockserver.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 *	2008-06-03 20:21
 */
public class SendMessageEvent implements IEvent {
	private final EventType type = EventType.MSG_SENT;
	private IMessage message;
	private IChannel client;
	private ISocketServer server;
	
	public SendMessageEvent(IMessage m,IChannel c){
		message = m;
		client = c;
		server = c.getServer();
	}
	
	public Object getSource() {
		return server;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public final IMessage getMessage() {
		return message;
	}

	public final IChannel getClient() {
		return client;
	}

	public final ISocketServer getServer() {
		return server;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer(1024);
		sb.append("send event. server=").append(server.getPort()).append(",client=");
		sb.append(client).append(",发送:");
		sb.append(message);
		return sb.toString();
	}
}
