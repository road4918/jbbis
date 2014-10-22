/**
 * 通用的收到消息事件定义
 */
package com.hzjbbis.fk.sockserver.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 *
 */
public class ReceiveMessageEvent implements IEvent {
	private final EventType type = EventType.MSG_RECV;
	private IMessage message;
	private IChannel client;
	private ISocketServer server;
	
	public ReceiveMessageEvent(IMessage m,IChannel c){
		message = m;
		client = c;
		server = c.getServer();
	}
	
	public Object getSource() {
		return null != server ? server : client;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public final IMessage getMessage() {
		return message;
	}
	
	public final void setMessage(IMessage msg){
		message = msg;
	}

	public final IChannel getClient() {
		return client;
	}

	public final ISocketServer getServer() {
		return server;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer(1024);
		if( null!= server )
			sb.append("recv event. server=").append(server.getPort()).append(",client=");
		else
			sb.append("recv event. client=");
		sb.append(client).append(",接收:");
		sb.append(message);
		return sb.toString();
	}
}
