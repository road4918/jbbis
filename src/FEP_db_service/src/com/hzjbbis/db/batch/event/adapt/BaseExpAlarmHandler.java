package com.hzjbbis.db.batch.event.adapt;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.db.batch.BaseBpEventHandler;
import com.hzjbbis.db.batch.event.BpExpAlarmEvent;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.zj.MessageZj;

public class BaseExpAlarmHandler extends BaseBpEventHandler {
	protected static final EventType type = EventType.BP_EXP_ALARM;

	public EventType type(){
		return type;
	}
	
	@Override
	public void handleEvent(IEvent event) {
		assert(event.getType() == type );
		BpExpAlarmEvent e = (BpExpAlarmEvent)event;
		handleExpAlarm(e.getService(),e.getMessage() );
	}

	public void handleExpAlarm(AsyncService service,MessageZj msg){
		
	}
}
