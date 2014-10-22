/**
 * 
 */
package com.hzjbbis.db.batch;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;

/**
 * @author bhw
 *
 */
public abstract class BaseBpEventHandler implements IEventHandler {
	protected AsyncService service;
	protected int key = 0;
	
	public abstract EventType type();
	public abstract void handleEvent(IEvent event);
	
	public void setKey(int key){
		this.key = key;
	}

	public void setService(AsyncService s){
		service = s;
	}
	
}
