/**
 * �¼���������ʱ �¼�������
 */
package com.hzjbbis.fk.common.events.adapt;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.events.event.EventHandleTimeoutAlarm;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;

/**
 * @author bao
 *
 */
public class EventHandleTimeoutAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(EventHandleTimeoutAdapt.class);
	private EventHandleTimeoutAlarm event;

	public void handleEvent(IEvent event) {
		this.event = (EventHandleTimeoutAlarm)event;
		process();
	}

	protected void process(){
		if(log.isInfoEnabled()){
			StringBuffer sb = new StringBuffer(1024);
			sb.append("�¼�����ʱ���¼����ͣ�").append(event.getTimeoutEvent().getType());
			sb.append(",begin=");
			Date date = new Date(event.getBeginTime());
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");
			sb.append(format.format(date)).append(",end=");
			sb.append(format.format(new Date(event.getEndTime()))).append("���¼����ݣ�");
			sb.append(event.getTimeoutEvent().toString());
			log.info(sb.toString());
		}
	}
}
