package com.hzjbbis.fk.common.spi;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.message.IMessage;

public interface IEvent {
	EventType getType();
	
	/**
	 * ������Ϣ������Դ������AsyncSocketClient����
	 * @return
	 */
	Object getSource();
	
	void setSource(Object src);
	IMessage getMessage();
}
