/**
 * 在异步通讯模式下，client不能直接发送数据。需要发送的数据，预先缓存到client对象中，
 * 然后异步检测是否可以写。在允许写的情况下，才可以执行socketChannel写操作。
 */
package com.hzjbbis.fk.sockserver.event;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bao
 *
 */
public class ClientWriteReqEvent implements IEvent {

	private final EventType type = EventType.CLIENT_WRITE_REQ;
	private IServerSideChannel client;

	public ClientWriteReqEvent(IServerSideChannel c){
		client = c;
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}

	public final IServerSideChannel getClient() {
		return client;
	}

	public Object getSource() {
		return client.getServer();
	}

	public void setSource(Object src) {
	}
	
	public IMessage getMessage(){
		return null;
	}
}
