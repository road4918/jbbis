/**
 * 任务数据上行业务处理事件
 */
package com.hzjbbis.db.batch.event;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.zj.MessageZj;

/**
 * @author bhw
 * 2008-10-23 00:45
 */
public class BpReadTaskEvent implements IEvent {
	private static final EventType type = EventType.BP_READ_TASK;
	private MessageZj message;
	private AsyncService service;
	
	public BpReadTaskEvent(AsyncService service,MessageZj msg){
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
