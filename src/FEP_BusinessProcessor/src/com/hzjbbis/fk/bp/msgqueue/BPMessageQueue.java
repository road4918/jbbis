/**
 * 业务处理器的上行报文队列。
 * 业务处理器下行，直接调用sendMessage方法。
 * 
 * 上行报文：
 * 报文队列的生产者（插入）：FEMessageEventHandle
 * 报文队列的消费者（取走）：????
 * 
 */
package com.hzjbbis.fk.bp.msgqueue;

import com.hzjbbis.db.DbMonitor;
import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.bp.feclient.FEChannelManage;
import com.hzjbbis.fk.common.queue.CacheQueue;
import com.hzjbbis.fk.common.spi.IMessageQueue;
import com.hzjbbis.fk.common.spi.IProfile;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 * 2008-10-21
 */
public class BPMessageQueue implements IMessageQueue, IProfile{
	private CacheQueue cacheQueue;	//spring 配置实现。
	private DbMonitor dbMonitor;
	private Runnable shutdownHook = new Runnable(){
		public void run(){
			BPMessageQueue.this.dispose();
		}
	};

	public void setCacheQueue( CacheQueue queue ){
		cacheQueue = queue;
		FasSystem.getFasSystem().addShutdownHook(shutdownHook);
	}

	//消息队列统一管理终端下行
	public boolean sendMessage(IMessage msg){
		IChannel channel = FEChannelManage.getChannel();
		channel.send(msg);
		return true;
	}
	
	//下面定义消息队列的方法
	public IMessage take(){
		if( null == dbMonitor || ( null!=dbMonitor && dbMonitor.isAvailable() ) )
			return cacheQueue.take();
		else
			return null;
	}
	
	public IMessage poll(){
		return cacheQueue.poll();
	}
	
	/**
	 * 当业务处理器收到通信前置机上行报文时，调用此函数，把上行消息放入队列。
	 * 业务处理器需要从这个队列中取数据进行处理。采用多线程方式进行处理。
	 * @param msg
	 */
	public void offer(IMessage msg){
		cacheQueue.offer(msg);
	}
	
	public int size(){
		return cacheQueue.size();
	}

	public final void setDbMonitor(DbMonitor dbMonitor) {
		this.dbMonitor = dbMonitor;
	}

	public String profile() {
		StringBuffer sb = new StringBuffer(256);
		sb.append("\r\n    <message-queue type=\"bp\">");
		sb.append("\r\n        <size>").append(size()).append("</size>");
		sb.append("\r\n    </message-queue>");
		return sb.toString();
	}
	
	public void dispose(){
		cacheQueue.dispose();
	}
}
