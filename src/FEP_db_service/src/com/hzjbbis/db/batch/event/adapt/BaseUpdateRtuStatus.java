package com.hzjbbis.db.batch.event.adapt;

import com.hzjbbis.db.batch.BaseBpEventHandler;
import com.hzjbbis.db.batch.event.FeUpdateRtuStatus;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;

public class BaseUpdateRtuStatus extends BaseBpEventHandler {
	private static final EventType type = EventType.FE_RTU_CHANNEL;
	
	@Override
	public void handleEvent(IEvent event) {
		assert(event.getType() == type );
		FeUpdateRtuStatus ev = (FeUpdateRtuStatus)event;
		service.addToDao(ev.getRtu(),key);
	}

	@Override
	public EventType type() {
		return type;
	}

}
