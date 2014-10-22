/**
 * SocketServer���ܿͻ��������¼���������
 */
package com.hzjbbis.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.sockserver.event.AcceptEvent;

/**
 * @author bao
 *
 */
public class AcceptEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(AcceptEventAdapt.class);
	private AcceptEvent event;

	public void handleEvent(IEvent event) {
		this.event = (AcceptEvent)event;
		process();
	}
	
	protected void process(){
		if( log.isInfoEnabled() )
			log.info("server["+event.getServer().getPort()+"] accept client["+event.getClient().getPeerIp()+"]");
	}
}
