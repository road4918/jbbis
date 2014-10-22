/**
 * ����ҵ��������ͨ��ǰ�û�֮�䱨���շ��¼�����
 * ���б��Ľ������ȼ����С�
 */
package com.hzjbbis.fk.bp.feclient;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.bp.msgqueue.BPMessageQueue;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;


/**
 * @author bhw
 *
 */
public class FEMessageEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(FEMessageEventHandler.class);	
	private BPMessageQueue msgQueue;	//spring ����ʵ�֡�
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}
	
	/**
	 * �յ�ͨ��ǰ�û������б��ġ�
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		MessageZj zjmsg = null;
		if (log.isDebugEnabled())
			log.debug("�յ�ͨ��ǰ�û������б���:"+msg.getRawPacketString());
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){
				//�ͻ�������ı���������Ӧ��
				return;		//�����������
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY ){
				zjmsg = mgate.getInnerMessage();
				_handleZjMessage(zjmsg,e);
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_SENDFAIL ){
				//GPRS��������ʧ�ܣ���Ҫ������ͨ������ͨ�����е��նˡ�
				zjmsg = mgate.getInnerMessage();
				//���ڲ�Э��ķ���ʧ�ܱ���ת��Ϊ�㽭��Լ����ʧ�ܱ��ġ�
				zjmsg = zjmsg.createSendFailReply();
				_handleZjMessage(zjmsg,e);
			}
			else if( mgate.getHead().getCommand() == MessageGate.REQ_MONITOR_RELAY_PROFILE ){
				//ǰ�û��������ص�profile
				String profile = FasSystem.getFasSystem().getProfile();
				MessageGate repMoniteProfile = MessageGate.createMoniteProfileReply(profile);
				msgQueue.sendMessage(repMoniteProfile);
				return;
			}
			else {
				//������������
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			_handleZjMessage((MessageZj)msg,e);
		}
	}
	
	private void _handleZjMessage(MessageZj zjmsg,ReceiveMessageEvent event){	
		//���Ľ������ж��У��Ա㷢�͸�ҵ��������
		msgQueue.offer(zjmsg);
	}
	
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		MessageZj zjmsg = null;
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				//�ͻ�������ı���������Ӧ��
				return;
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				zjmsg = mgate.getInnerMessage();
			}
			else
				return;
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			zjmsg = (MessageZj)msg;
		}
		if( null == zjmsg )
			return;
		if (log.isDebugEnabled())
			log.debug("��ͨѶǰ�û���������:"+zjmsg.getRawPacketString());				
	}

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
}
