/**
 * TCP socket服务器停止事件
 */
package com.hzjbbis.fk.sockserver.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bao
 *
 */
public class ServerStoppedEvent implements IEvent {
	private final EventType type = EventType.SERVERSTOPPED;
	private ISocketServer server = null;
	
	public ServerStoppedEvent(ISocketServer s){
		server = s;
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}
	
	public final ISocketServer getServer(){
		return server;
	}
	
	public Object getSource() {
		return server;
	}

	public void setSource(Object src) {
	}
	
	public IMessage getMessage(){
		return null;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer(128);
		sb.append("server stopped event. server=").append(server);
		return sb.toString();
	}
	
}
