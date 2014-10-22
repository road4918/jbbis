/**
 * 网关消息队列。
 * 上行报文必须先进入消息队列，由消息队列进行发送。
 * 前置机下行报文也通过消息队列方法直接发送出去。
 * 消息队列实现思路：
 * 	1）每个网关前置机服务与唯一一个消息队列关联。通过spring配置实现。
 *  2）上行消息进入消息队列，需要按照优先级排队；
 *  3）每次允许发送给前置机（前置机连接通知以及前置机对应client发送完毕通知），按照优先级取下一报文
 *  4）消息队列如果满了，则把优先级最低部分滚动到缓存文件。
 *  5）前置机连接成功事件通知时，优先发送队列消息，在空闲时，发送缓存文件内容。
 *  6）缓存文件最大为40M，文件名称为cache-port-i.txt 。其中i为文件序号。
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
	private boolean noConvert = false;		//是否直接上行浙江规约原始报文.
	private boolean oneClientPerIP = true;
	
	//内部属性，用于控制轮询client连接对象
	private int curIndex = 0;
	/**
	 * 一个网关对多个前置机支持：每个网关终端服务端口与前置机IP关联。
	 * 本消息队列－终端服务－前置机服务 三者一一对应。必须通过spring配置实现。
	 */
	private CacheQueue queue;	//spring 配置实现。
	
	//前置机的client连接数很少，所以采用List
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
		log.info("前置机连接成功:"+client);
	}
	
	public void onFrontEndClosed(IChannel client){
		synchronized(clients){
			clients.remove(client);
		}
		log.info("前置机断开连接:"+client);
	}
	
	private IServerSideChannel findIdleClient(){
		if( clients.size()==0 )
			return null;
		synchronized(clients){
			IServerSideChannel client;

			//简单循环队列算法
			//从curIndex开始检测到最后
			for(int i=curIndex; i<clients.size(); i++ ){
				client = (IServerSideChannel)clients.get(i);
				if( client.sendQueueSize()==0 ){
					curIndex = i+1;
					if( curIndex>= clients.size() )
						curIndex = 0;
					return client;
				}
			}
			//从0到curIndex
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
			//不能发送。
			queue.offer(msg);
			if( trace.isEnabled() )
				trace.trace( "MessageQueue:客户端requestNum==0, msg="+msg);
			return;
		}
		
		if( noConvert || msg instanceof MessageGate )
			client.send( msg );
		else{
			//把浙江规约报文转换成网关规约，发送给前置机。
			MessageGate gateMsg = new MessageGate();
			gateMsg.setUpInnerMessage(msg);
			client.send(gateMsg);
		}
	}
	
	/**
	 * 从队列取上行消息。如果没有消息，则返回NULL。
	 * @return
	 */
	public IMessage pollUpMessage(){
		return queue.poll();
	}
	
	/**
	 * 从队列取上行消息。如果没有消息，则等待。
	 * @return
	 */
	public IMessage takeUpMessage(){
		return queue.take();
	}

	/**
	 * 查询RTUA对应的client，直接发送给终端。
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
				log.warn("RTUChannelManager.getClient 处理>N毫秒, time="+(n2-n1));
			if( null == client ){
				log.error("sendDownMessage。无连接，发送失败。rtu="+HexDump.toHex(msg.head.rtua));
			}
			else{
				client.send(message);
				n2 = System.currentTimeMillis();
				if( n2-n1>20 )
					log.warn("client.toSend(message) 处理>N毫秒, time="+(n2-n1));
				return true;
			}
		}
		else{
			//不支持消息发送。
			log.warn("不支持消息发送。msgtype="+message.getMessageType());
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
