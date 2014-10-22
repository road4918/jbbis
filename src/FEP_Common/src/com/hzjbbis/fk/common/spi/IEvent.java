package com.hzjbbis.fk.common.spi;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.message.IMessage;

public interface IEvent {
	EventType getType();
	
	/**
	 * 返回消息产生的源，例如AsyncSocketClient对象。
	 * @return
	 */
	Object getSource();
	
	void setSource(Object src);
	IMessage getMessage();
}
