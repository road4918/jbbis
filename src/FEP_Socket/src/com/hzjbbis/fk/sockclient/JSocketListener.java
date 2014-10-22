/**
 * 侦听JSocket的收、发消息，以及Jsocket连接或者关闭的状态。
 */
package com.hzjbbis.fk.sockclient;

import com.hzjbbis.fk.message.IMessage;

/**
 * @author hbao
 *
 */
public interface JSocketListener {
	void onReceive(JSocket client,IMessage msg);
	void onSend(JSocket client,IMessage msg);
	void onConnected(JSocket client);
	void onClose(JSocket client);
}
