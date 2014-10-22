package com.hzjbbis.fk.common.events.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;

public class MessageParseErrorEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(MessageParseErrorEventAdapt.class);
//	private IEvent event;
	
	public void handleEvent(IEvent event) {
		log.warn(event);
	}

}
