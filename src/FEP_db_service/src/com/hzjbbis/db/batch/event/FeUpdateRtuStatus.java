package com.hzjbbis.db.batch.event;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.IMessage;

/**
 * 更新前置机的通道、流量等信息。
 * @author bhw
 *
 */
public class FeUpdateRtuStatus implements IEvent {
	private static final EventType type = EventType.FE_RTU_CHANNEL;
	private AsyncService service;
	private Object rtu;
	
	public FeUpdateRtuStatus(AsyncService service,Object rtu){
		this.service = service;
		this.rtu = rtu;
	}
	
	public AsyncService getService(){
		return service;
	}

	public Object getRtu(){
		return rtu;
	}
	
	public IMessage getMessage() {
		return null;
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
