/**
 * 事件处理超时告警事件
 */
package com.hzjbbis.fk.common.events.event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bao
 *
 */
public class EventHandleTimeoutAlarm implements IEvent {
	private final EventType type = EventType.SYS_EVENT_PROCESS_TIMEOUT;
	private Object source;
	private IEvent event;
	private String threadName;
	private long beginTime,endTime;
	private List<StackTraceElement> stackTraces = new ArrayList<StackTraceElement>();
	
	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public EventHandleTimeoutAlarm(IEvent ev){
		event = ev;
		threadName = Thread.currentThread().getName();
	}

	public Object getSource() {
		return source;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
		source = src;
	}

	public void setType(EventType type) {
	}

	/**
	 * 获取处理超时的事件
	 * @return
	 */
	public IEvent getTimeoutEvent(){
		return event;
	}
	
	public IMessage getMessage(){
		return null;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer(1024);
		sb.append("事件处理超时。类型：").append(event.getType());
		sb.append(",thread=").append(threadName);
		sb.append(",begin=");
		Date date = new Date(this.getBeginTime());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");
		sb.append(format.format(date)).append(",end=");
		sb.append(format.format(new Date(this.getEndTime()))).append("。事件内容：");
		sb.append(event.toString());
		sb.append("。StackTraceElement:");
		for(StackTraceElement st: stackTraces ){
			sb.append("\r\n\t").append(st.toString());
		}
		return sb.toString();
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	
	public void setStackTraceElement(StackTraceElement [] trace){
		if( null == trace || trace.length == 0 )
			return;
		for(StackTraceElement st: trace){
			stackTraces.add(st);
		}
	}
}
