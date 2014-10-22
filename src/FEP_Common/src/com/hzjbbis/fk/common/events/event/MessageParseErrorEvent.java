/**
 * Message�����ڶ�ȡ���������ݣ�������Ϣ���ʱ�����¼���
 * ���ڴ�ӡ��Щ��Ϣ���ݲ��ܽ�����sourceΪclient����toString�����ṩIP��ַ��Ϣ��
 */
package com.hzjbbis.fk.common.events.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 * 2006-06-06 21:57
 */
public class MessageParseErrorEvent implements IEvent {
	private final EventType type = EventType.MSG_PARSE_ERROR;
	private IMessage message;
	private Object source;		//��Ϣ�����Ӧ����ԴClientChannel
	private String info;

	public MessageParseErrorEvent(IMessage msg){
		message = msg;
		source = msg.getSource();
	}

	public MessageParseErrorEvent(IMessage msg,String info){
		message = msg;
		source = msg.getSource();
		this.info = info;
	}
	
	public IMessage getMessage() {
		return message;
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
	
	public String toString(){
		StringBuffer sb = new StringBuffer(512);
		sb.append("MessageParseErrorEvent,source=").append(source);
		sb.append(",packet=").append(message.getRawPacketString());
		if( null != info )
			sb.append(",info=").append(info);
		return sb.toString();
	}
}
