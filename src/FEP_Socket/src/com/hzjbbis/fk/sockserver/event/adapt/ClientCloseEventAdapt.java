/**
 * 客户端关闭事件处理器适配器
 */
package com.hzjbbis.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.sockserver.event.ClientCloseEvent;

/**
 * @author bao
 *
 */
public class ClientCloseEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ClientCloseEventAdapt.class);
	protected ClientCloseEvent event;

	public void handleEvent(IEvent ev) {
		event = (ClientCloseEvent)ev;
		process(event);
	}

	protected void process(ClientCloseEvent event){
		if( log.isInfoEnabled() )
			log.info("server["+event.getServer().getPort()+"] close client["+event.getClient().getPeerIp()+"]");
	}

}
