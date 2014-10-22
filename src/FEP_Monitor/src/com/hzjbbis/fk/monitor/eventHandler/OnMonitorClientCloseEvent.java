package com.hzjbbis.fk.monitor.eventHandler;

import com.hzjbbis.fk.monitor.biz.HandleRtuTrace;
import com.hzjbbis.fk.sockserver.event.ClientCloseEvent;
import com.hzjbbis.fk.sockserver.event.adapt.ClientCloseEventAdapt;

/**
 * 监控管理需要处理监控客户端连接断开事件。
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
