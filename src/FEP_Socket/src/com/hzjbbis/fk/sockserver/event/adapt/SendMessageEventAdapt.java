package com.hzjbbis.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;

public class SendMessageEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(SendMessageEventAdapt.class);
	private SendMessageEvent event;

	public void handleEvent(IEvent event) {
		this.event = (SendMessageEvent)event;
		process();
	}

	protected void process(){
		if( log.isInfoEnabled() )
			log.info(event);
	}
}
