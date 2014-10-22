package com.hzjbbis.db.batch.event.adapt;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.db.batch.BaseBpEventHandler;
import com.hzjbbis.db.batch.event.BpReadTaskEvent;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.zj.MessageZj;

public class BaseReadTaskHandler extends BaseBpEventHandler {
	protected static final EventType type = EventType.BP_READ_TASK;
	
	@Override
	public void handleEvent(IEvent event) {
		assert(event.getType() == type );
		BpReadTaskEvent e = (BpReadTaskEvent)event;
		handleReadTask(e.getService(),e.getMessage() );
	}
	
	public void handleReadTask(AsyncService service,MessageZj msg){
		
	}

	@Override
	public EventType type() {
		return type;
	}

}
