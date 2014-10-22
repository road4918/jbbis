/**
 * 应用系统级别命令。
 */
package com.hzjbbis.fk.monitor.client.biz;

import java.nio.ByteBuffer;

import com.hzjbbis.fk.monitor.MonitorCommand;
import com.hzjbbis.fk.monitor.message.MonitorMessage;
import com.hzjbbis.fk.sockclient.JSocket;

/**
 * @author bhw
 *
 */
public class SystemCommand {
	public void shutdown(JSocket client){
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(MonitorCommand.CMD_SYS_STOP);
		ByteBuffer body = ByteBuffer.allocate(0);
		msg.setBody(body);
		client.sendMessage(msg);
	}
}
