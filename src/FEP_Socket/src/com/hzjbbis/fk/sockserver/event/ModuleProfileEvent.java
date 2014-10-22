/**
 * ÿ���Ӳ�����SocketServerͳ������
 */
package com.hzjbbis.fk.sockserver.event;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 *
 */
public class ModuleProfileEvent implements IEvent {
	private static final Logger log = Logger.getLogger(ModuleProfileEvent.class);
	private EventType type = EventType.MODULE_PROFILE;
	private IModule module = null;
	private long lastReceiveTime=0,lastSendTime=0;			//�����ա���ʱ��
	private long totalRecvMessages=0,totalSendMessages=0;	//�ܹ��ա�����Ϣ����
	private int msgRecvPerMinute=0,msgSendPerMinute=0;		//ÿ�����ա������ĸ���
	private int clientSize = 0;

	public ModuleProfileEvent(IModule m){
		module = m;
		this.lastReceiveTime = m.getLastReceiveTime();
		this.lastSendTime = m.getLastSendTime();
		this.totalRecvMessages = m.getTotalRecvMessages();
		this.totalSendMessages = m.getTotalSendMessages();
		this.msgRecvPerMinute = m.getMsgRecvPerMinute();
		this.msgSendPerMinute = m.getMsgSendPerMinute();
		if( m instanceof ISocketServer ){
			clientSize = ((ISocketServer)m).getClientSize();
		}
	}
	
	public Object getSource() {
		return module;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public void setType(EventType type) {
	}

	public final long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public final long getLastSendTime() {
		return lastSendTime;
	}

	public final long getTotalRecvMessages() {
		return totalRecvMessages;
	}

	public final long getTotalSendMessages() {
		return totalSendMessages;
	}

	public final int getMsgRecvPerMinute() {
		return msgRecvPerMinute;
	}

	public final int getMsgSendPerMinute() {
		return msgSendPerMinute;
	}

	public final int getClientSize() {
		return clientSize;
	}
	
	public IMessage getMessage(){
		return null;
	}
	
	public String toString(){
		String ret =  module.profile();
		if( log.isDebugEnabled() )
			log.debug(ret);
		return ret;
	}
}
