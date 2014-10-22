/**
 * ǰ�û����Ӷ�Tcp Socket�������¼�������
 * ���ܸ�����
 * 		�����յ�ǰ�û����б��ġ��ն����б��ķ��ͳɹ��¼���
 *    ǰ�û����б���ͨ��MessageQueue���󷽷�ֱ�ӷ��͸��նˣ�
 *    �ն����б��ĳɹ��¼����򵥴�ӡ��־���޽�һ����������
 * ����ʵ�֣�
 * BasicEventHook�����ࡣ
 * override handleEvent���������ReceiveMessageEvent��SendMessageEvent�ر���
 * ע�������spring�����ļ��У�source�������������ǰ�û��˷���ӿڵ�SocketServer����
 */
package com.hzjbbis.fk.gate.event;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.gate.MessageQueue;
import com.hzjbbis.fk.gate.PrefixRtuManage;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.sockserver.event.AcceptEvent;
import com.hzjbbis.fk.sockserver.event.ClientCloseEvent;
import com.hzjbbis.fk.tracelog.TraceLog;

/**
 * @author bhw
 * 2008-06-06 11:03
 */
public class GateFEEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(GateFEEventHandler.class);
	private static final TraceLog trace = TraceLog.getTracer(GateFEEventHandler.class);
	private MessageQueue queue;
	private boolean noConvert = false;		//�Ƿ�ֱ�������㽭��Լԭʼ����.
	
	public boolean start() {
		return super.start();
	}

	public void setQueue(MessageQueue queue) {
		this.queue = queue;
	}
	
	/**
	 * ���ظ÷�����
	 */
	public void handleEvent(IEvent e) {
		/** ����ǰ�û��˷����յ����ģ�����ֱ�ӷ��͸��նˡ�
		 *  ����ǰ�û�����Accept��client���͡���Ҫ�ŵ�ǰ�û����ж��С�
		 *  	1����ǰ�û����ӵ����أ�֪ͨ���ж��з������б��ģ�
		 *  	2����ǰ�û���Ӧclient�ɹ��������б��ģ�֪ͨ���ж��м������ͣ�
		 */
		boolean processed = false;
		if( e.getType() == EventType.MSG_RECV ){
			//�������ع�Լ���ģ���Ҫת�����㽭��Լ���ſ��Է��͸��㽭�նˡ�
			IMessage msg = e.getMessage();
			if( msg.getMessageType() == MessageType.MSG_GATE ){
				MessageGate mgate = (MessageGate)msg;
				//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
				short cmd = mgate.getHead().getCommand();
				if( cmd == MessageGate.CMD_GATE_HREQ ){
					//��ȡ�ͻ�������ı�������
					ByteBuffer data = mgate.getData();
					int numPackets = data.remaining()<4 ? -1 : data.getInt()+1;
					IServerSideChannel client = (IServerSideChannel)msg.getSource();
					synchronized(client){
						client.setRequestNum(numPackets);
					}
					//Ӧ������
					MessageGate hreply = MessageGate.createHReply();
					client.send(hreply);
					if( trace.isEnabled() )
						trace.trace( "�յ��ͻ��������ģ�requestNum="+numPackets );
					return;		//�����������
				}
				else if( cmd == MessageGate.CMD_GATE_REQUEST || cmd == MessageGate.CMD_WRAP_ZJ ){
					msg = mgate.getInnerMessage();
					if( null != msg && log.isDebugEnabled() )
						log.debug("ǰ�û���������:"+msg);
				}
				else if( cmd == MessageGate.REQ_MONITOR_RELAY_PROFILE ){
					//ǰ�û��������ص�profile
					String profile = FasSystem.getFasSystem().getProfile();
					MessageGate repMoniteProfile = MessageGate.createMoniteProfileReply(profile);
					queue.offerUpMessageInQueue(repMoniteProfile);
					return;
				}
				else {
					processed = ! processed ;
				}
			}
			try{
				MessageZj zjmsg = (MessageZj)msg;
				if( null != zjmsg ){
					//���������ļ��ĸ߿��ն�������Ҫ����ǰ���ַ�
					zjmsg.setPrefix(PrefixRtuManage.getInstance().getRtuPrefix(zjmsg.head.rtua));
					queue.sendDownMessage(zjmsg);
				}
			}catch(Exception exp){
				log.warn(exp.getLocalizedMessage(),exp);
			}
			//���ԣ������Զ�Ӧ��ԭ��Ϣ���ء�
			processed = ! processed ;
		}
		else if( e.getType() == EventType.MSG_SENT ){
			IMessage msg = e.getMessage();
			if( log.isDebugEnabled() )
				log.debug("��ǰ�û����ͱ��ĳɹ�:"+msg);
			IServerSideChannel client = (IServerSideChannel)msg.getSource();
			//���client�����������Ƿ�ݼ���0��
			int numReq = client.getRequestNum();
			if( numReq == 0 ){
				//���ܷ��͡�
				if( trace.isEnabled() )
					trace.trace( "�ͻ���requestNum==0, msg="+msg );
				return;
			}
			msg = queue.pollUpMessage();
			
			if( null != msg && trace.isEnabled() )
				trace.trace("ʣ��ɷ��ͱ��ģ�"+ numReq+",��ǰ����msg="+msg );
			else
				trace.trace("ʣ��ɷ��ͱ��ģ�"+ numReq+",��ǰ����Ϣ����." );

			if( null != msg ){
				if( noConvert )
					client.send(msg);
				else{
					//���㽭��Լ����ת�������ع�Լ�����͸�ǰ�û���
					MessageGate gateMsg = new MessageGate();
					gateMsg.setUpInnerMessage((MessageZj)msg);
					client.send(gateMsg);
				}
			}
			processed = true;
		}
		else if( e.getType() == EventType.ACCEPTCLIENT ){
			AcceptEvent ae = (AcceptEvent)e;
			queue.onFrontEndConnected(ae.getClient());
			processed = true;
		}
		else if( e.getType() == EventType.CLIENTCLOSE ){
			ClientCloseEvent ce = (ClientCloseEvent)e;
			queue.onFrontEndClosed(ce.getClient());
		}
		if( !processed )
			super.handleEvent(e);
	}

	public boolean isNoConvert() {
		return noConvert;
	}

	public void setNoConvert(boolean noConvert) {
		this.noConvert = noConvert;
	}
}
