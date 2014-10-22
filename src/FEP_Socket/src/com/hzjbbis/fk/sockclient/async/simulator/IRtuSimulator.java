/**
 * 终端模拟器接口定义
 */
package com.hzjbbis.fk.sockclient.async.simulator;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.sockclient.async.JAsyncSocket;

/**
 * @author hbao
 *
 */
public interface IRtuSimulator {
	int  getRtua();
	void setRtua(int rtua);
	void onConnect(JAsyncSocket client);
	void onClose(JAsyncSocket client);
	void onReceive(JAsyncSocket client,IMessage message);
	void onSend(JAsyncSocket client,IMessage message);
	void sendLogin();
	void sendHeart();
	void sendTask();
}
