/**
 * 收到客户端报文时，需要确定是否自动应答。
 * 根据报文类型，生产自动应答报文。
 */
package com.hzjbbis.fk.gate.event.autoreply;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.zj.MessageZj;

/**
 * @author bhw
 * 2008－06－06 10：40
 */
public class AutoReply {
	
	public static final IMessage reply(IMessage msg){
		if( msg.getMessageType() == MessageType.MSG_ZJ )
			return AutoReplyMessageZj.reply((MessageZj)msg);
		return null;
	}
}
