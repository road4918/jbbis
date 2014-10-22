/**
 * 服务器接受客户端连接事件
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
public class AcceptEvent implements IEvent {
	private final EventType type = EventType.ACCEPTCLIENT;
	private ISocketServer server;
	private IServerSideChannel client;

	public AcceptEvent(IServerSideChannel c){
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

	public String toString(){
		return "AcceptEvent,server="+server.getPort()+",client="+client.getPeerAddr();
	}
}
