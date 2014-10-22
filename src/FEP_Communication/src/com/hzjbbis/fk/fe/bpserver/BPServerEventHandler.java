/**
 * ҵ������Socket���������¼�������
 * ���ܸ�����
 * 		�����յ�ҵ������(BP)���б��ġ����б��ķ��ͳɹ��¼���
 *      ҵ���������б���ͨ��FEMessageQueue���󷽷�ֱ�ӷ��͸����أ�������ֱ�ӷ��͸��նˣ�
 * ����ʵ�֣�
 * BasicEventHook�����ࡣ
 * override handleEvent���������ReceiveMessageEvent��SendMessageEvent�ر���
 * ע�������spring�����ļ��У�source���������ҵ����������ӿڵ�SocketServer����
 */
package com.hzjbbis.fk.fe.bpserver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.fe.msgqueue.FEMessageQueue;
import com.hzjbbis.fk.fe.msgqueue.MessageDispatch2Bp;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.sockserver.event.AcceptEvent;
import com.hzjbbis.fk.sockserver.event.ClientCloseEvent;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;
import com.hzjbbis.fk.tracelog.TraceLog;

/**
 * @author bhw
 * 2008-06-06 11:03
 */
public class BPServerEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(BPServerEventHandler.class);
	private static final TraceLog trace = TraceLog.getTracer(BPServerEventHandler.class);
	private FEMessageQueue msgQueue;
	private boolean noConvert = false;		//�Ƿ�ֱ�������㽭��Լԭʼ����.
	private boolean dispatchRandom = true;
	
	//�ڲ�����
	private List<IServerSideChannel> bpClients = Collections.synchronizedList(new ArrayList<IServerSideChannel>());

	public BPServerEventHandler(){
	}
	
	@Override
	public boolean start() {
		return super.start();
	}
	
	public void setMsgQueue(FEMessageQueue queue) {
		this.msgQueue = queue;
		msgQueue.setDispatchRandom(dispatchRandom);
		msgQueue.setNoConvert(noConvert);
	}
	
	public FEMessageQueue getMsgQueue(){
		return msgQueue;
	}
	
	/**
	 * ���ظ÷�����
	 */
	public void handleEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			//���յ�ҵ���������б���
			onRecvMessage( (ReceiveMessageEvent)e);
		}
		else if( e.getType() == EventType.MSG_SENT ){
			//���ɹ��ѱ��ķ��͸�ҵ������
			onSendMessage( (SendMessageEvent)e );
		}
		else if( e.getType() == EventType.ACCEPTCLIENT ){
			//���������쳣�Ͽ������CLIENTCLOSE�¼����ܲ��ᷢ����������bpClients�������
			//ÿ��ɾ��1�����ɴﵽ��Ч����������
			for(int i=0; i<bpClients.size(); i++ ){
				try{
					IServerSideChannel client = bpClients.get(i);
					if( System.currentTimeMillis()-client.getLastIoTime() > 1000*60*30 ){
						bpClients.remove(i);
						if( trace.isEnabled() )
							trace.trace("garbage client removed:"+client);
						break;
					}
				}catch(Exception exp){
					break;
				}
			}
			AcceptEvent ae = (AcceptEvent)e;
			bpClients.add(ae.getClient());
			//msgQueue.onBpClientConnected(ae.getClient());
		}
		else if( e.getType() == EventType.CLIENTCLOSE ){
			ClientCloseEvent ce = (ClientCloseEvent)e;
			bpClients.remove(ce.getClient());
			msgQueue.onBpClientClosed(ce.getClient());
		}
		else if( e.getType() == EventType.MSG_SEND_FAIL ){
			//��client���رգ���sendList�б��ģ��������ա�
			msgQueue.pushBack(e.getMessage());
		}
		else
			super.handleEvent(e);
	}

	/**
	 * �յ�ҵ�����������б���
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		//�������ع�Լ���ģ���Ҫת�����㽭��Լ���ſ��Է��͸��㽭�նˡ�
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				IServerSideChannel client = (IServerSideChannel)msg.getSource();
				//only business processor can send heart-beat to FE. Manufacture module send MessageZj.
				//If there is a client channel in bpClients, then fire msgQueue onConnect event
				if( bpClients.remove(client) )
					msgQueue.onBpClientConnected(client);
				
				//��ȡ�ͻ�������ı�������
				ByteBuffer data = mgate.getData();
				int numPackets = data.remaining()<4 ? -1 : data.getInt();
				synchronized(client){
					client.setRequestNum(numPackets);
				}
				//Ӧ������
				MessageGate hreply = MessageGate.createHReply();
				client.send(hreply);
				return;		//�����������
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				MessageZj zjmsg = mgate.getInnerMessage();
				//ָ������ͨ�����У���Ҫ��sendMessage�����ж�MessageGate��
				zjmsg.setPeerAddr(mgate.getSource().getPeerAddr());	//�����ͳɹ���zjmsg.peerAddr����Ϊ���б�����Դ��ַ
				boolean success = msgQueue.sendMessage(mgate);
				if( success && log.isDebugEnabled() )
					log.debug("ҵ��������������:"+zjmsg);
			}
			else if( mgate.getHead().getCommand() == MessageGate.REP_MONITOR_RELAY_PROFILE ){
				String bpProfile = new String(mgate.getData().array());
				FasSystem.getFasSystem().addBizProcessorProfile(e.getClient().getPeerAddr(), bpProfile);
				return;
			}
			else if( mgate.getHead().getCommand() == MessageGate.CMD_WRAP_ZJ ){
				//�㽭��Լֱ֡�����У������ǳ��ҽ���ģ�顣ֱ����������
				MessageZj zjmsg = mgate.getInnerMessage();
				if( null == zjmsg )
					return;
				//�����ͳɹ���zjmsg.peerAddr����Ϊ���б�����Դ��ַ
				zjmsg.setPeerAddr(mgate.getSource().getPeerAddr());
				boolean success = msgQueue.sendMessage(zjmsg);
				if( success && log.isDebugEnabled() )
					log.info("���ҽ���ģ����������:"+zjmsg);
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			MessageZj zjmsg = (MessageZj)msg;
			boolean success = msgQueue.sendMessage(zjmsg);
			if( success && log.isDebugEnabled() )
				log.debug("ҵ��������������:"+zjmsg);
		}
	}
	
	/**
	 * ��ҵ���������б��ĳɹ���
	 * @param e
	 */
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		//�ر�ע�⣺����ǳ����Զ��屨�ķ��ͳɹ�������Ҫ�������͡�
		//��Ϊ�յ������Զ��屨��ʱ�����Զ���ס����ͨ���볧�ұ����ϵ��Ӧ�������������ϵ�Զ����͸�����ģ�顣
		if( msg instanceof MessageZj ){
			MessageZj zjmsg = (MessageZj)msg;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
				if( log.isDebugEnabled() )
					log.debug("�����ҽ���ģ�鷢�ͱ��ĳɹ�:"+zjmsg.getRawPacketString());
				return;
			}
		}
		if( log.isDebugEnabled() )
			log.debug("��ҵ���������ͱ��ĳɹ�:"+msg);
		//���ķ��͵�ҵ����������Ҫ���������͡�
		//2009��1��18 modified by bhw��֧�ֶ�ҵ�����������յ��зַ���
		if( dispatchRandom ){
			IServerSideChannel client = (IServerSideChannel)e.getClient();
			trySendNextPacket(client);
		}
		else{
			//���յ��о���ַ���ҵ������
			trySendNextPacketByA1();
		}
	}
	
	/**
	 * ���ն�����Ϣ�ĵ�������зַ���ҵ��������
	 */
	private void trySendNextPacketByA1(){
		MessageZj msg = (MessageZj)msgQueue.poll();
		if( null == msg )
			return;
		IServerSideChannel client = MessageDispatch2Bp.getInstance().getBpChannel(msg.head.rtua_a1);
		if( null == client ){
			msgQueue.pushBack(msg);
			return;
		}
		if( 0 >= client.getRequestNum() )
			return;
		boolean success = false;
		if( noConvert ){
			success = client.send(msg);
		}
		else{
			//���㽭��Լ����ת�������ع�Լ�����͸�ǰ�û���
			MessageGate gateMsg = new MessageGate();
			gateMsg.setUpInnerMessage(msg);
			success = client.send(gateMsg);
		}
		if( !success ){
			msgQueue.pushBack(msg);
		}
	}
	
	private void trySendNextPacket(IServerSideChannel client){
		//���client�����������Ƿ�ݼ���0��
		if( 0 >= client.getRequestNum() ){
			//���ܷ��͡�
			return;
		}
		IMessage msg = msgQueue.poll();
		if( null != msg ){
			boolean success = false;
			if( noConvert )
				success = client.send(msg);
			else{
				//���㽭��Լ����ת�������ع�Լ�����͸�ǰ�û���
				MessageGate gateMsg = new MessageGate();
				gateMsg.setUpInnerMessage(msg);
				success = client.send(gateMsg);
			}
			if( !success ){
				msgQueue.pushBack(msg);
			}
		}
	}
	
	public boolean isNoConvert() {
		return noConvert;
	}

	public void setNoConvert(boolean noConvert) {
		this.noConvert = noConvert;
		if( null != msgQueue )
			msgQueue.setNoConvert(noConvert);
	}
	
	public void setDispatchRandom(boolean dispRandom ){
		dispatchRandom = dispRandom;
		if( null != msgQueue )
			msgQueue.setDispatchRandom(dispatchRandom);
	}
}
