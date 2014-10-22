package com.hzjbbis.fk.sockclient.async.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.IMessage;

public class ClientConnectedEvent implements IEvent {
	private final EventType type = EventType.CLIENT_CONNECTED;
	private ISocketServer server;
	private IChannel client;

	public ClientConnectedEvent(ISocketServer s,IChannel c){
		server = s;
		client = c;
	}
	
	public IMessage getMessage() {
		return null;
	}

	public Object getSource() {
		return server;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public ISocketServer getServer() {
		return server;
	}

	public IChannel getClient() {
		return client;
	}

	public String toString(){
		return "ClientConnectedEvent,client="+client.getPeerAddr();
	}
}
