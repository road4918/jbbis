package com.hzjbbis.fk.common.spi;

import com.hzjbbis.fk.message.IMessage;

public interface IClientModule extends IModule {
	boolean sendMessage(IMessage msg);
}
