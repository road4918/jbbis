/**
 * �첽TCP ���������� ע�⣺�رճ�ʱ��û���κ�IO��SocketChannel��
 * SocketServer����һ��IoThreadPool����IoThreadPool���������SocketIoThread��
 * ÿ��SocketChannelֻ�ܹ���ĳ��SocketIoThreadִ�����в��������ǵ�JDK bug�����ܿ��̡߳�
 */
package com.hzjbbis.fk.sockserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.simpletimer.ITimerFunctor;
import com.hzjbbis.fk.common.simpletimer.TimerData;
import com.hzjbbis.fk.common.simpletimer.TimerScheduler;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseSocketServer;
import com.hzjbbis.fk.sockserver.event.AcceptEvent;
import com.hzjbbis.fk.sockserver.event.ModuleProfileEvent;
import com.hzjbbis.fk.sockserver.event.ServerStartedEvent;
import com.hzjbbis.fk.sockserver.event.ServerStoppedEvent;
import com.hzjbbis.fk.sockserver.io.SocketIoThreadPool;
import com.hzjbbis.fk.utils.State;


/**
 * @author bhw
 * 2008-05-22
 */
public class TcpSocketServer extends BaseSocketServer implements IModule, ITimerFunctor{
	//��̬����
	private static final Logger log = Logger.getLogger(TcpSocketServer.class);

	//����������
	private boolean oneIpLimit = false;	//�Ƿ�����ͬһ��IP��ַֻ����һ�����ӡ������س���������

	//��������
	protected ServerSocketChannel ssc;
	protected Selector selector;		//�����첽Accept�ͻ�������
	private volatile State state = State.STOPPED; //������״̬
	protected Map<String,AsyncSocketClient> map = Collections.synchronizedMap(new HashMap<String,AsyncSocketClient>(1024*50));
	private AcceptThread acceptThread = null;
	private SocketIoThreadPool ioPool = null;

	public TcpSocketServer(){
	}
	

	public String getModuleType() {
		return IModule.MODULE_TYPE_SOCKET_SERVER;
	}

	public boolean isActive() {
		return state.isActive();
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("A-TCP(").append(port).append(")");
		return sb.toString();
	}
	/**
	 * ����TCP socket����
	 * @return
	 */
	public boolean start(){
		if( !state.isStopped() ){
			log.warn("socket server["+port+"]��ֹͣ״̬��������������");
			return false;
		}
		if( ioThreadSize<=0 )
			ioThreadSize = Runtime.getRuntime().availableProcessors()*2;
		state = State.STARTING;

		ioPool = new SocketIoThreadPool(port,ioThreadSize,ioHandler);
		ioPool.start();
		
		acceptThread = new AcceptThread();
		acceptThread.start();
		int cnt = 1000;
		while( state.isStarting() && cnt-->0 )
		{
			Thread.yield();
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){}
		}
		//������ʱ����
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,60));	//��ʱ��0��ÿ���Ӷ�ʱ��
		TimerScheduler.getScheduler().addTimer(new TimerData(this,1,timeout));	//��ʱ��1��clientTimeoutTask

		log.info("TCP���������ɹ���"+port+"��");
		GlobalEventHandler.postEvent(new ServerStartedEvent(this));
		return true;
	}
	
	public void onTimer(int timerID){
		if( 0==timerID ){	//ÿ���Ӷ�ʱ��
			long now = System.currentTimeMillis();
			if( now-lastReceiveTime < 60*1000 || now-lastSendTime < 60*1000 )
				GlobalEventHandler.postEvent(new ModuleProfileEvent(this));
			synchronized(statisticsRecv){
				msgRecvPerMinute = 0;
			}
			synchronized(statisticsSend){
				msgSendPerMinute = 0;
			}
		}
		else if( 1==timerID ){
			//�ͻ��˳�ʱ��鶨ʱ��
			ArrayList<AsyncSocketClient>list = new ArrayList<AsyncSocketClient>(map.values());
			long now = System.currentTimeMillis();
			int closedCount = 0;
			for(AsyncSocketClient client: list){
				if( now-client.getLastReadTime() > timeout*1000 ){
					forceCloseClient(client);
					closedCount++;
				}
			}
			if( closedCount>0 )
				log.warn("TCP����["+name+"]��ʱ�رտͻ���������="+closedCount);
		}
	}
	
	/**
	 * ֹͣ����TCP socket����
	 */
	public void stop(){
		if( !state.isRunning() )
			return;
		state = State.STOPPING; // stopping

		//ֹͣServer socket�����߳�
		acceptThread.interrupt();
		int cnt = 500;
		while( acceptThread.isAlive() && cnt-->0 ){
			Thread.yield();
			try{
				acceptThread.join(20);
			}catch(InterruptedException e){}
		}
		acceptThread = null;
		
		//ֹͣ����client socket IO �߳�
		ioPool.stop();
		
		TimerScheduler.getScheduler().removeTimer(this, 0);
		TimerScheduler.getScheduler().removeTimer(this, 1);
		
		state = State.STOPPED;
		log.info("TCP����ֹͣ��"+port+"��");
		GlobalEventHandler.postEvent(new ServerStoppedEvent(this));
	}
	
	private class AcceptThread extends Thread {
		public AcceptThread(){
			super("TcpServer-"+port+"-AcceptThread");
		}
		public void run() {
			//����Socket������
			try {
				ssc = ServerSocketChannel.open();
				ssc.socket().setReuseAddress(true);
				InetSocketAddress addr = null;
				if( null == ip )
					addr = new InetSocketAddress(port);
				else
					addr = new InetSocketAddress(ip,port);
				ssc.socket().bind(addr);
				ssc.configureBlocking(false);
			} catch (Exception exp) {
				//log it;
				log.fatal("TCPServer start failed. "+exp.getLocalizedMessage()+",port="+port);
				return;
			}
			
			log.info("server[" + port + "]listen thread is running");

			try{
				selector = Selector.open();
				ssc.register(selector, SelectionKey.OP_ACCEPT);
			}
			catch(Exception e){
				log.error("socketserver �����߳��쳣��selectorOpen)"+e.getMessage());
				state = com.hzjbbis.fk.utils.State.STOPPED;
				return;
			}
			state = com.hzjbbis.fk.utils.State.RUNNING;
			
			long sign = System.currentTimeMillis();
			int cnt = 0;
			int times = 0;
			//tryAcceptÿ���60�뻽��һ�Σ��ṩ������socket client��ʱ�����
			while ( state != com.hzjbbis.fk.utils.State.STOPPING){
				try{
					tryAccept();
					
					//���ü������Լ�ʱ�ӣ������ѭ��
					cnt++;
					if( cnt>= 200 ){
						long now = System.currentTimeMillis();
						if( now-sign < 1000 ){
							log.warn("server[" + port + "]Accept thread���ܽ�����ѭ����");
						}
						cnt = 0;
						sign = System.currentTimeMillis();
					}
					
					//���socket client �Ƿ�ʱ��û��IO�� 1���Ӽ��һ�Ρ�
					if( times++ >= 10 ){
						checkTimeout();
					}
				}
				catch(Exception e){
					log.error("server[" + port + "]AcceptThread�쳣:"+e.getLocalizedMessage(),e);
				}
			}
			try{
				ssc.close();
			}
			catch(IOException ioe){
				log.warn("ssc.close�쳣��"+ioe.getLocalizedMessage());
			}
			ssc = null;
			try{
				selector.close();
			}
			catch(IOException ioe){
				log.warn("selector.close�쳣��"+ioe.getLocalizedMessage());
			}
			selector = null;
			log.info("server[" + port + "]listen thread is stopping");
		}
		
		private void tryAccept() throws IOException,ClosedSelectorException{
			//�ر�ע�⣺JDK��selector����ȱ�ݡ�����selector(1000)���������ӳ�1��ż�⵽�����¼���
			//ʵ���ϣ��ͻ����Ѿ�������������˸�Ϊselector(50)��ȷ��Ѹ�ټ�⵽OP_ACCEPT
			int n = selector.select(50);
			if( n<=0 ){
				//���ﲻ���κ����飬�������selectedKeys��
			}
			Set<SelectionKey> set = selector.selectedKeys();
			for( SelectionKey key: set ){
				if (key.isAcceptable()){
					try{
					doAccept();
					}catch(Exception e){
						log.warn("doAccept()�쳣��"+e.getLocalizedMessage(),e);
						key.cancel();
					}
				}
				else{
					//��Ӧ�ó����������
					log.warn("��Acceptʱ��SelectionKey�Ƿ���"+key);
					key.cancel();
				}
			}
			set.clear();
		}
		
		private void doAccept()throws IOException{
			SocketChannel channel = ssc.accept();
			channel.socket().setReceiveBufferSize(bufLength);
			channel.socket().setSendBufferSize(bufLength);
			channel.configureBlocking(false);
			AsyncSocketClient client = new AsyncSocketClient(channel,TcpSocketServer.this);
			String clientKey = client.getPeerAddr();
			if( oneIpLimit )
				clientKey = client.getPeerIp();
			map.put(clientKey, client);
			
			//����Accept��SocketChannel���䵽ĳ���ض��̡߳�
			ioPool.acceptNewClient(client);
			
			//�ѽ��ܿͻ��������¼�����ȫ���¼�������
			GlobalEventHandler.postEvent(new AcceptEvent(client));
		}
	}
	
	/**
	 * ���ͻ��������Ͽ�����ʱ��client�Ѿ���SocketIoThread�رա�
	 * �ص�����������Map�������socketChannel��
	 * @param client
	 */
	public void removeClient(IServerSideChannel client){
		String clientKey = client.getPeerAddr();
		if( oneIpLimit )
			clientKey = client.getPeerIp();
		map.remove(clientKey);
		super.removeClient(client);
	}
	
	/**
	 * �ӷ������������Ͽ����ӡ�Ӧ�ó�������socket client��ʱ��û��IO�������������������رա�
	 * �����ر�client������Ҫͨ��SocketIoThread���о���رն�������Դ��
	 * @param client
	 */
	public void forceCloseClient(AsyncSocketClient client){
		removeClient(client);
		client.getIoThread().closeClientRequest(client);
	}

	private void checkTimeout(){
		//ע�⣺�����map�Ѷ���refference������������������map��ͻ��
		ArrayList<AsyncSocketClient> list = new ArrayList<AsyncSocketClient>(map.values());
		long now = System.currentTimeMillis();
		for( AsyncSocketClient client: list){
			if( now-client.getLastReadTime() > timeout*1000 ){
				forceCloseClient(client);
			}
		}
	}
	
	public int getClientSize(){
		return map.size();
	}
	
	/**
	 * ���ر�TCP�����������пͻ������Ӷ���
	 */
	public IServerSideChannel[] getClients(){
		return map.values().toArray(new IServerSideChannel[0]);
	}
	
	public boolean isOneIpLimit() {
		return oneIpLimit;
	}
	public void setOneIpLimit(boolean oneIpLimit) {
		this.oneIpLimit = oneIpLimit;
	}

}

