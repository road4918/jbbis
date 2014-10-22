package com.hzjbbis.fk.monitor.client;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.monitor.MonitorCommand;
import com.hzjbbis.fk.monitor.client.biz.ClientHandleFile;
import com.hzjbbis.fk.monitor.message.MonitorMessage;
import com.hzjbbis.fk.sockclient.JSocket;
import com.hzjbbis.fk.sockclient.JSocketListener;

public class MonitorSocketListener implements JSocketListener {
	private static final Logger log = Logger.getLogger(MonitorSocketListener.class);

	public IMonitorReplyListener replyListener = null;

	public void onClose(JSocket client) {
		if( null != replyListener )
			replyListener.onClose();
		log.info("��ط������ӶϿ�:"+client);
	}

	public void onConnected(JSocket client) {
		if( null != replyListener )
			replyListener.onConnect();
		log.info("��ط������ӳɹ�:"+client);
	}

	public void onReceive(JSocket client,IMessage message) {
		MonitorMessage msg = (MonitorMessage)message;
		msg.resetMessageState();		//�Ա���Ϣ�����ٴζ�����д��������Ϣ״̬Ϊ����ϡ�
		ByteBuffer body = null;
		switch(msg.getCommand()){
		case MonitorCommand.CMD_CONFIG_LIST:		//�����ļ��б�
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onListConfig(result);
			}
			break;
		case MonitorCommand.CMD_LOG_LIST:		//�����ļ��б�
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onListLog(result);
			}
			break;
		case MonitorCommand.CMD_GET_FILE:	//�����ļ�
			body = ClientHandleFile.getHandleFile().getFile(msg.getBody());
			if( null == body ){
				if( null != replyListener )
					replyListener.onGetFile();
				return;
			}
			msg.setBody(body);
			client.sendMessage(msg);
			break;
		case MonitorCommand.CMD_PUT_FILE:
			body = ClientHandleFile.getHandleFile().putFile(msg.getBody());
			if( null == body ){
				if( null != replyListener )
					replyListener.onPutFile();
				return;
			}
			msg.setBody(body);
			client.sendMessage(msg);
			break;
		case MonitorCommand.CMD_SYS_PROFILE:
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onSystemProfile(result);
			}
			break;
		case MonitorCommand.CMD_MODULE_PROFILE:
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onModuleProfile(result);
			}
			break;
		case MonitorCommand.CMD_EVENT_HOOK_PROFILE:
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onEventHookProfile(result);
			}
			break;
		case MonitorCommand.CMD_GATHER_PROFILE:
			body = msg.getBody();
			{
				String profile = new String(body.array());
				if( null != replyListener )
					replyListener.onMultiSysProfile(profile);
			}
			break;
		case MonitorCommand.CMD_TRACE_IND:
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onRtuMessageInd(result);
			}
			break;
		}
	}

	public void onSend(JSocket client,IMessage msg) {

	}

}
