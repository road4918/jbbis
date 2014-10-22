package com.hzjbbis.fk.gate;

import com.hzjbbis.fk.common.spi.socket.IChannel;

public class RTUChannel {
	public int rtua;
	public IChannel client;
	
	public RTUChannel(int rtu, IChannel channel){
		rtua = rtu;
		client = channel;
	}
}
