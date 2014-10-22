package com.hzjbbis.fk.monitor.eventHandler;

import com.hzjbbis.fk.monitor.biz.HandleRtuTrace;
import com.hzjbbis.fk.sockserver.event.ClientCloseEvent;
import com.hzjbbis.fk.sockserver.event.adapt.ClientCloseEventAdapt;

/**
 * ��ع�����Ҫ�����ؿͻ������ӶϿ��¼���
 * @author hbao
 *
 */
public class OnMonitorClientCloseEvent extends ClientCloseEventAdapt {

	@Override
	protected void process(ClientCloseEvent event) {
		super.process(event);
		HandleRtuTrace.getHandleRtuTrace().onClientClose(event);
	}

}
