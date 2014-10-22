package com.hzjbbis.fk.sockclient.async.eventhandler;

import java.util.HashMap;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.sockclient.async.event.adapt.OnClientClosed;
import com.hzjbbis.fk.sockclient.async.event.adapt.OnClientConnected;
import com.hzjbbis.fk.sockclient.async.event.adapt.OnClientRecvMsg;
import com.hzjbbis.fk.sockclient.async.event.adapt.OnClientSendMsg;

public class AsyncSocketEventHandler extends BasicEventHook {
	private IEventHandler listener;
	public void init(){
		// set include
		if( null == include ){
			include = new HashMap<EventType,IEventHandler>();
			include.put(EventType.CLIENT_CONNECTED, new OnClientConnected());
			include.put(EventType.CLIENTCLOSE, new OnClientClosed());
			include.put(EventType.MSG_RECV, new OnClientRecvMsg());
			include.put(EventType.MSG_SENT, new OnClientSendMsg());
		}
		super.init();
	}
	
	@Override
	public void handleEvent(IEvent event) {
		super.handleEvent(event);
		if( null != listener )
			listener.handleEvent(event);
	}

	public IEventHandler getListener() {
		return listener;
	}

	public void setListener(IEventHandler listener) {
		this.listener = listener;
	}
	
}
