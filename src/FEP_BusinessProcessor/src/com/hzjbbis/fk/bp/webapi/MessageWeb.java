package com.hzjbbis.fk.bp.webapi;

import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;

public class MessageWeb extends MessageGate {

	public MessageWeb(){
		type = MessageType.MSG_WEB;
	}
}
