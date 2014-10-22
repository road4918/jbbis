package com.hzjbbis.fk.message;

public interface IMessageCreator {
	IMessage create();
	IMessage createHeartBeat(int reqNum);
}
