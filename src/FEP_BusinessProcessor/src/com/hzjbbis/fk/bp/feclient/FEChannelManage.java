/**
 * ����ͨ��ǰ�û���ͨ����
 */
package com.hzjbbis.fk.bp.feclient;

import com.hzjbbis.fk.clientmod.ClientModule;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseClientChannel;




/**
 * @author bhw
 *
 */
public class FEChannelManage {
	private static BaseClientChannel channel;
	
	/**
	 * ��ӣ�ͨ��ǰ�û��Ŀͻ�������
	 * @param feClient
	 */
	public static void addClient(ClientModule feClient ){
		channel = feClient.getSocket();
	}
	
	public static IChannel getChannel(){
		return channel;
	}
	
}
