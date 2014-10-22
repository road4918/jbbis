/**
 * �ͻ��˳�ʱ��û��IO����ʱ���¼����� ������
 */
package com.hzjbbis.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.sockserver.event.ClientTimeoutEvent;

/**
 * @author bao
 *
 */
public class ClientTimeoutEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ClientTimeoutEventAdapt.class);
	private ClientTimeoutEvent event;

	public void handleEvent(IEvent event) {
		this.event = (ClientTimeoutEvent)event;
		process();
	}

	protected void process(){
		if( log.isInfoEnabled() )
			log.info("client["+event.getClient().getPeerIp()+"]��ʱ��û��IO�����رա�");
	}
}
