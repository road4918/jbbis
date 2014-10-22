/**
 * socket服务停止事件适配器
 */
package com.hzjbbis.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.sockserver.event.ServerStoppedEvent;

/**
 * @author bao
 *
 */
public class ServerStopEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ServerStopEventAdapt.class);
	private ServerStoppedEvent event;

	public void handleEvent(IEvent event) {
		this.event = (ServerStoppedEvent)event;
		process();
	}
	
	protected void process(){
		if(log.isInfoEnabled())
			log.info(event);
	}
}
