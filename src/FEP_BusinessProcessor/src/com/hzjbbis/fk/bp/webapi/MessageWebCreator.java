package com.hzjbbis.fk.bp.webapi;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.IMessageCreator;

public class MessageWebCreator implements IMessageCreator {

	/**
	 * ��վ��ҵ��������web-bp��֮������
	 */
	public IMessage createHeartBeat(int reqNum) {
		return MessageWeb.createHRequest(reqNum);
	}

	public IMessage create() {
		return new MessageWeb();
	}

}
