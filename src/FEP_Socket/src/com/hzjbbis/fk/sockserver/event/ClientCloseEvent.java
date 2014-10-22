/**
 * TCP服务器的客户端连接断开或者关闭的事件。
 */
package com.hzjbbis.fk.sockserver.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bao
 *
 */
public class ClientCloseEvent implements IEvent {
	private final EventType type = EventType.CLIENTCLOSE;
	private ISocketServer server;
	private IServerSideChannel client;

	public ClientCloseEvent(IServerSideChannel c){
		server = c.getServer();
		client = c;
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}

	public final ISocketServer getServer() {
		return server;
	}

	public final IServerSideChannel getClient() {
		return client;
	}

	public Object getSource() {
		return server;
	}

	public void setSource(Object src) {
	}
	
	public IMessage getMessage(){
		return null;
	}
}
