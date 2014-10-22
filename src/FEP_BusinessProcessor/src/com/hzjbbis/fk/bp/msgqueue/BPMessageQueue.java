/**
 * ҵ�����������б��Ķ��С�
 * ҵ���������У�ֱ�ӵ���sendMessage������
 * 
 * ���б��ģ�
 * ���Ķ��е������ߣ����룩��FEMessageEventHandle
 * ���Ķ��е������ߣ�ȡ�ߣ���????
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
	private CacheQueue cacheQueue;	//spring ����ʵ�֡�
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

	//��Ϣ����ͳһ�����ն�����
	public boolean sendMessage(IMessage msg){
		IChannel channel = FEChannelManage.getChannel();
		channel.send(msg);
		return true;
	}
	
	//���涨����Ϣ���еķ���
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
	 * ��ҵ�������յ�ͨ��ǰ�û����б���ʱ�����ô˺�������������Ϣ������С�
	 * ҵ��������Ҫ�����������ȡ���ݽ��д������ö��̷߳�ʽ���д���
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
