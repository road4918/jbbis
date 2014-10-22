package com.hzjbbis.fk.bp.feclient;

import com.hzjbbis.fk.clientmod.ClientModule;
import com.hzjbbis.fk.message.IMessage;

public class IntfChannelManage {
	private static IntfChannelManage instance;
	//	���������ԣ�ͨ��SPRING���á�
	ClientModule client;
	
	private IntfChannelManage(){
		instance = this;
	}
	public static IntfChannelManage getInstance(){
		if( null == instance )
			instance = new IntfChannelManage();
		return instance;
	}
	public ClientModule getClient() {
		return client;
	}

	public void setClient(ClientModule client) {
		this.client = client;
	}
	public void sendMessage(IMessage msg){
		this.getClient().getSocket().send(msg);
	}
		
}
