/**
 * 服务器定时统计数据汇总事件适配器。
 */
package com.hzjbbis.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;

/**
 * @author bhw
 *
 */
public class ModuleProfileEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ModuleProfileEventAdapt.class);
	private IEvent event;

	public void handleEvent(IEvent event) {
		this.event = event;
		process();
	}
	
	protected void process(){
		if( log.isInfoEnabled() ){
			log.info(event);
		}
	}

}
