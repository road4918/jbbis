package com.hzjbbis.fk.gate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.hzjbbis.fk.common.spi.socket.IChannel;

public class RTUChannelManager {
	private static Map<Integer,IChannel> pool = Collections.synchronizedMap(new HashMap<Integer,IChannel>(8012));
	
	public static final void addClient(int rtua,IChannel client){
		pool.put(rtua, client);
	}

	public static final void removeClient(int rtua){
		pool.remove(rtua);
	}
	
	public static final IChannel getClient(int rtua){
		return pool.get(rtua);
	}
}
