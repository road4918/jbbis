package com.hzjbbis.fk.message.gate;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.IMessageCreator;

public class MessageGateCreator implements IMessageCreator {

	/**
	 * ǰ�û��ڲ���BP-FE-GATE��֮��������ͬʱҲ�������������ģ�
	 */
	public IMessage createHeartBeat(int reqNum) {
		return MessageGate.createHRequest(reqNum);
	}

	public IMessage create() {
		return new MessageGate();
	}

}
