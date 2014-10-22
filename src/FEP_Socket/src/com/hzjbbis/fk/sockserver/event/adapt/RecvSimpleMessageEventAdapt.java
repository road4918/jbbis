/**
 * �յ�����Ϣ��SimpleMessage���¼�������
 */
package com.hzjbbis.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;

/**
 * @author bao
 *
 */
public class RecvSimpleMessageEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(RecvSimpleMessageEventAdapt.class);
	private ReceiveMessageEvent event;

	public void handleEvent(IEvent event) {
		this.event = (ReceiveMessageEvent)event;
		process();
	}

	protected void process(){
		if( log.isInfoEnabled() )
			log.info(event);
	}
}
