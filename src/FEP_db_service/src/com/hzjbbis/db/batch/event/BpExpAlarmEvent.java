package com.hzjbbis.db.batch.event;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.zj.MessageZj;

public class BpExpAlarmEvent implements IEvent {
	private static final EventType type = EventType.BP_EXP_ALARM;
	private MessageZj message;
	private AsyncService service;
	
	public BpExpAlarmEvent(AsyncService service,MessageZj msg){
		this.service = service;
		this.message = msg;
	}
	
	public MessageZj getMessage() {
		return message;
	}

	public AsyncService getService(){
		return service;
	}
	
	public AsyncService getSource() {
		return service;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

}
