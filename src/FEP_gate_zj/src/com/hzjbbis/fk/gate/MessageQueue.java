/**
 * ������Ϣ���С�
 * ���б��ı����Ƚ�����Ϣ���У�����Ϣ���н��з��͡�
 * ǰ�û����б���Ҳͨ����Ϣ���з���ֱ�ӷ��ͳ�ȥ��
 * ��Ϣ����ʵ��˼·��
 * 	1��ÿ������ǰ�û�������Ψһһ����Ϣ���й�����ͨ��spring����ʵ�֡�
 *  2��������Ϣ������Ϣ���У���Ҫ�������ȼ��Ŷӣ�
 *  3��ÿ�������͸�ǰ�û���ǰ�û�����֪ͨ�Լ�ǰ�û���Ӧclient�������֪ͨ�����������ȼ�ȡ��һ����
 *  4����Ϣ����������ˣ�������ȼ���Ͳ��ֹ����������ļ���
 *  5��ǰ�û����ӳɹ��¼�֪ͨʱ�����ȷ��Ͷ�����Ϣ���ڿ���ʱ�����ͻ����ļ����ݡ�
 *  6�������ļ����Ϊ40M���ļ�����Ϊcache-port-i.txt ������iΪ�ļ���š�
 */
package com.hzjbbis.fk.gate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.queue.CacheQueue;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.tracelog.TraceLog;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 * 2008-06-03 23:01
 */
public class MessageQueue {
	private static final Logger log = Logger.getLogger(MessageQueue.class);
	private static final TraceLog trace = TraceLog.getTracer(MessageQueue.class);
	private boolean noConvert = false;		//�Ƿ�ֱ�������㽭��Լԭʼ����.
	private boolean oneClientPerIP = true;
	
	//�ڲ����ԣ����ڿ�����ѯclient���Ӷ���
	private int curIndex = 0;
	/**
	 * һ�����ضԶ��ǰ�û�֧�֣�ÿ�������ն˷���˿���ǰ�û�IP������
	 * ����Ϣ���У��ն˷���ǰ�û����� ����һһ��Ӧ������ͨ��spring����ʵ�֡�
	 */
	private CacheQueue queue;	//spring ����ʵ�֡�
	
	//ǰ�û���client���������٣����Բ���List
	private List<IChannel> clients = new ArrayList<IChannel>();

	public void onFrontEndConnected(IChannel client){
		synchronized(clients){
			boolean addok = false;
			if( oneClientPerIP ){
				String ip = client.getPeerIp();
				IChannel c;
				for( int i=0; i<clients.size(); i++ ){
					c = clients.get(i);
					if( ip.equalsIgnoreCase(c.getPeerIp()) ){
						clients.set(i, client);
						addok = true;
						break;
					}
				}
			}
			if( !addok )
				clients.add(client);
		}
		log.info("ǰ�û����ӳɹ�:"+client);
	}
	
	public void onFrontEndClosed(IChannel client){
		synchronized(clients){
			clients.remove(client);
		}
		log.info("ǰ�û��Ͽ�����:"+client);
	}
	
	private IServerSideChannel findIdleClient(){
		if( clients.size()==0 )
			return null;
		synchronized(clients){
			IServerSideChannel client;

			//��ѭ�������㷨
			//��curIndex��ʼ��⵽���
			for(int i=curIndex; i<clients.size(); i++ ){
				client = (IServerSideChannel)clients.get(i);
				if( client.sendQueueSize()==0 ){
					curIndex = i+1;
					if( curIndex>= clients.size() )
						curIndex = 0;
					return client;
				}
			}
			//��0��curIndex
			for(int i=0; i<curIndex; i++ ){
				client = (IServerSideChannel)clients.get(i);
				if( client.sendQueueSize()==0 ){
					curIndex = i+1;
					if( curIndex>= clients.size() )
						curIndex = 0;
					return client;
				}
			}
			return null;
		}
	}
	
	public void offerUpMessageInQueue(IMessage msg){
		IServerSideChannel client = findIdleClient();
		if( null == client ){
			queue.offer(msg);
			return;
		}
		
		int numReq = client.getRequestNum();
		if( numReq == 0 ){
			//���ܷ��͡�
			queue.offer(msg);
			if( trace.isEnabled() )
				trace.trace( "MessageQueue:�ͻ���requestNum==0, msg="+msg);
			return;
		}
		
		if( noConvert || msg instanceof MessageGate )
			client.send( msg );
		else{
			//���㽭��Լ����ת�������ع�Լ�����͸�ǰ�û���
			MessageGate gateMsg = new MessageGate();
			gateMsg.setUpInnerMessage(msg);
			client.send(gateMsg);
		}
	}
	
	/**
	 * �Ӷ���ȡ������Ϣ�����û����Ϣ���򷵻�NULL��
	 * @return
	 */
	public IMessage pollUpMessage(){
		return queue.poll();
	}
	
	/**
	 * �Ӷ���ȡ������Ϣ�����û����Ϣ����ȴ���
	 * @return
	 */
	public IMessage takeUpMessage(){
		return queue.take();
	}

	/**
	 * ��ѯRTUA��Ӧ��client��ֱ�ӷ��͸��նˡ�
	 * @param message
	 * @return
	 */
	public boolean sendDownMessage(IMessage message){
		if( null == message ){
			log.warn("sendDownMessage(null)");
			return false;
		}
		long n1 = System.currentTimeMillis();
		if( message instanceof MessageZj){
			MessageZj msg = (MessageZj)message;
			IChannel client = RTUChannelManager.getClient(msg.head.rtua);
			long n2 = System.currentTimeMillis();
			if( n2-n1>20 )
				log.warn("RTUChannelManager.getClient ����>N����, time="+(n2-n1));
			if( null == client ){
				log.error("sendDownMessage�������ӣ�����ʧ�ܡ�rtu="+HexDump.toHex(msg.head.rtua));
			}
			else{
				client.send(message);
				n2 = System.currentTimeMillis();
				if( n2-n1>20 )
					log.warn("client.toSend(message) ����>N����, time="+(n2-n1));
				return true;
			}
		}
		else{
			//��֧����Ϣ���͡�
			log.warn("��֧����Ϣ���͡�msgtype="+message.getMessageType());
		}
		return false;
	}

	public CacheQueue getQueue() {
		return queue;
	}

	public void setQueue(CacheQueue queue) {
		this.queue = queue;
	}

	public boolean isNoConvert() {
		return noConvert;
	}

	public void setNoConvert(boolean noConvert) {
		this.noConvert = noConvert;
	}
	
	
}
