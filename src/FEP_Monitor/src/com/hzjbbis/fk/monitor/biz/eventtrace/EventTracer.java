package com.hzjbbis.fk.monitor.biz.eventtrace;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventTrace;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.monitor.MonitorCommand;
import com.hzjbbis.fk.monitor.message.MonitorMessage;
import com.hzjbbis.fk.utils.CalendarUtil;
/**
 * �㽭�����¼����ٽӿ�ʵ�֡�
 * @author hbao
 *
 */
public class EventTracer implements IEventTrace {
	private ArrayList<IChannel> monitorClients = new ArrayList<IChannel>();
	private int[] rtus = null;

	public EventTracer(IChannel client){
		monitorClients.add(client);
	}
	
	public void addClient(IChannel client){
		synchronized(monitorClients){
			monitorClients.remove(client);
			monitorClients.add(client);
		}
	}
	
	public int removeClient(IChannel client){
		synchronized(monitorClients){
			monitorClients.remove(client);
			if( monitorClients.size() == 0 )
				rtus = null;
			return monitorClients.size();
		}
	}
	
	public void traceRtus(int [] tobeTraced ){
		if( null == tobeTraced )
			return;
		if( null == rtus ){
			rtus = tobeTraced;
			return;
		}
		int count = 0;	//�ܼƸ���������ȥ���ظ�RTUA
		int [] tp = new int[tobeTraced.length];
		for(int i=0; i<tobeTraced.length; i++ ){
			boolean found = false;
			for(int j=0; j<rtus.length; j++ ){
				if( rtus[j] == tobeTraced[i] ){
					found = true;
					break;
				}
			}
			if( !found ){
				tp[count] = tobeTraced[i];
				count++;
			}
		}
		if( 0 == count )
			return;
		int[] newRtus = new int[rtus.length+count];
		System.arraycopy(rtus, 0, newRtus, 0, rtus.length);
		System.arraycopy(tp, 0, newRtus, rtus.length, count);
		rtus = newRtus;
	}
	
	/**
	 * Ŀǰ֧�ָ�����Ϣ�Ľ����뷢���¼��������¼����ڼ�ع��������û�����塣
	 */
	public void traceEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			IMessage msg = e.getMessage();
			IChannel client = (IChannel)msg.getSource();
			//����������Ϣ����Ҫ���г�ȡ�㽭��Լ��Ϣ��
			MessageZj message = null;
			if( msg instanceof MessageZj ){
				message = (MessageZj)msg;
			}
			else if( msg.getMessageType() == MessageType.MSG_GATE ){
				message = (MessageZj)((MessageGate)msg).getInnerMessage();
				if( null == message )
					return;
				message.setIoTime(msg.getIoTime());
			}
			else
				return;
			//ƥ���ص�RTUA
			if( ! _isMonited(message.head.rtua) )
				return;
			StringBuffer sb = new StringBuffer(400);
			sb.append("�յ�:��").append(client.getServer().getName()).append("��,ʱ��=");
			sb.append(CalendarUtil.getMilliDateTimeString(message.getIoTime()));
			sb.append(", ����=").append(message.getRawPacketString()).append("\r\n");
			_sendIndication(sb.toString());
		}
		else if( e.getType() == EventType.MSG_SENT ){
			IMessage msg = e.getMessage();
			IChannel client = (IChannel)msg.getSource();
			//����������Ϣ����Ҫ���г�ȡ�㽭��Լ��Ϣ��
			MessageZj message = null;
			if( msg instanceof MessageZj ){
				message = (MessageZj)msg;
			}
			else if( msg.getMessageType() == MessageType.MSG_GATE ){
				message = (MessageZj)((MessageGate)msg).getInnerMessage();
				if( null == message )
					return;
				message.setIoTime(msg.getIoTime());
			}
			else
				return;
			//ƥ���ص�RTUA
			if( ! _isMonited(message.head.rtua) )
				return;
			
			StringBuffer sb = new StringBuffer(400);
			sb.append("����:��").append(client.getServer().getName()).append("��,ʱ��=");
			sb.append(CalendarUtil.getMilliDateTimeString(message.getIoTime()));
			sb.append(", ����=").append(message.getRawPacketString()).append("\r\n");
			_sendIndication(sb.toString());
		}
	}
	
	/**
	 * ���ع���ͻ��˷��ͼ�ظ�����Ϣ��
	 * @param sb
	 */
	private void _sendIndication(String info){
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(MonitorCommand.CMD_TRACE_IND);
		ByteBuffer body = ByteBuffer.wrap(info.getBytes());
		msg.setBody(body);
		synchronized(monitorClients){
			for( IChannel client: monitorClients )
				client.send(msg);
		}
	}
	
	private boolean _isMonited(int rtua){
		try{
			for(int i=0; i<rtus.length; i++ ){
				if( rtus[i] == rtua )
					return true;
			}
		}catch(Exception e){}
		return false;
	}

}
