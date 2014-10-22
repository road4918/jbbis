package com.hzjbbis.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;

public class ReceiveMessageEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ReceiveMessageEventAdapt.class);

	public void handleEvent(IEvent ev) {
		ReceiveMessageEvent event = (ReceiveMessageEvent)ev;
		try{
			process(event);
		}catch(Exception exp){
			log.error("������Ϣ�¼������쳣��"+exp.getLocalizedMessage(),exp);
		}
	}

	protected void process(ReceiveMessageEvent event){
		if( log.isInfoEnabled() )
			log.info(event);
	}
}
