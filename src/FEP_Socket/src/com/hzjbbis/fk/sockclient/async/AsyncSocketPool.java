package com.hzjbbis.fk.sockclient.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.socket.IClientIO;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.IMessageCreator;
import com.hzjbbis.fk.sockclient.async.event.ClientConnectedEvent;
import com.hzjbbis.fk.sockserver.io.SocketIoThreadPool;
import com.hzjbbis.fk.utils.State;

/**
 * �첽socketͨѶ���ӳء�AsyncSocketPool������SocketServer�����������첽SocketChannel��
 * ����˼·��
 * 	ÿ��socketChannel������
 * @author hbao
 *
 */
public class AsyncSocketPool implements ISocketServer {
	private static final Logger log = Logger.getLogger(AsyncSocketPool.class);
	private static int sequence = 1;
	//����������
	private String name = "�첽socket���ӳ�";
	private String peerIp;
	private int peerPort;
	private int clientSize = 1;
	private int ioThreadSize = 2;	 //socket IO �̳߳ش�С
	private int bufLength = 256;	 //socket�ͻ��˻�������С
	//SocketChannel��д�ӿ�ʵ����·����
	private IClientIO ioHandler = null;	//SocketChannel����д�����ӿ�
	//��socket ���յ��ı��Ķ������͡�
	private String messageClass = "com.hzjbbis.fk.sockserver.message.SimpleMessage";
	private IMessageCreator messageCreator;
	private int timeout = 30*60;		//Ĭ��30 ���ӳ�ʱ

	/**
	 * ͨѶ��ʽ���壺
	 * 01:����; 02:GPRS;  03:DTMF;  04:Ethernet;
	 * 05:����; 06:RS232; 07:CSD;   08:Radio; 	09:CDMA;
	 */
	private String txfs="02";
	//��������
	private Class<IMessage> msgClassObject;
	
	//ͳ������,��ʼ���Զ�Ϊ0
	private long lastReceiveTime=0,lastSendTime=0;			//�����ա���ʱ��
	private long totalRecvMessages=0,totalSendMessages=0;	//�ܹ��ա�����Ϣ����
	private int msgRecvPerMinute=0,msgSendPerMinute=0;		//ÿ�����ա������ĸ���

	//��������
	private volatile State state = State.STOPPED; //������״̬
	protected List<JAsyncSocket> clients = Collections.synchronizedList(new ArrayList<JAsyncSocket>(1024));
	private SocketIoThreadPool ioPool = null;
	private AsyncSocketConnectThread connectThread;

	@SuppressWarnings("unchecked")
	public boolean start() {
		try{
			if( null == messageCreator ){
				msgClassObject = (Class<IMessage>)Class.forName(messageClass);
				msgClassObject.newInstance();
			}
			else {
				if( null != messageClass && messageClass.length()>0 ){
					try{
						msgClassObject = (Class<IMessage>)Class.forName(messageClass);
						msgClassObject.newInstance();
					}catch(Exception e){
						log.error("AsyncSocketPool���󴴽�����,��������ļ���"+e.getLocalizedMessage(),e);
					}
				}
			}
		}
		catch(Exception exp){
			log.error("AsyncSocketPool���󴴽�����,��������ļ���"+exp.getLocalizedMessage(),exp);
			System.exit(-1);
		}
		if( !state.isStopped() ){
			log.warn("AsyncSocketPool ��ֹͣ״̬��������������");
			return false;
		}
		if( ioThreadSize<=0 )
			ioThreadSize = Runtime.getRuntime().availableProcessors()*2;
		state = State.STARTING;
		
		ioPool = new SocketIoThreadPool(peerPort,ioThreadSize,ioHandler);
		ioPool.start();
		
		connectThread = new AsyncSocketConnectThread();
		connectThread.start();
		int cnt = 1000;
		while(! state.isRunning() && cnt-->0 )
		{
			Thread.yield();
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){}
		}
		//����client socket channel
		for(int i=0;i<clientSize;i++){
			clients.add(new JAsyncSocket(this));
		}
		log.info("AsyncSocketPool�����ɹ�");
//		GlobalEventHandler.postEvent(new ServerStartedEvent(this));
		return true;
	}

	public void stop() {
		if( !state.isRunning() )
			return;
		state = State.STOPPING; // stopping

		//ֹͣServer socket�����߳�
		connectThread.interrupt();
		int cnt = 500;
		while( connectThread.isAlive() && cnt-->0 ){
			Thread.yield();
			try{
				connectThread.join(20);
			}catch(InterruptedException e){}
		}
		connectThread = null;
		
		//ֹͣ����client socket IO �߳�
		ioPool.stop();
		ioPool = null;
		
		clients.clear();
		state = State.STOPPED;
		log.info("AsyncSocketPool ֹͣ");
	}
	
	public IMessage createMessage() {
		if( null != messageCreator )
			return messageCreator.create();
		try{
			return msgClassObject.newInstance();
		}
		catch(Exception exp){
			return null;
		}
	}

	public int getBufLength() {
		return this.bufLength;
	}

	public int getClientSize() {
		return clientSize;
	}
	
	/**
	 * �����첽TCP�ͻ������ӳ�����Socket Client��������
	 */
	public IServerSideChannel[] getClients(){
		return clients.toArray(new IServerSideChannel[0]);
	}

	public long getLastReceiveTime() {
		return this.lastReceiveTime;
	}

	public long getLastSendTime() {
		return this.lastSendTime;
	}

	public int getMaxContinueRead() {
		return 10;
	}

	public int getMsgRecvPerMinute() {
		return this.msgRecvPerMinute;
	}

	public int getMsgSendPerMinute() {
		return this.msgSendPerMinute;
	}

	public int getPort() {
		return 0;
	}

	public long getTotalRecvMessages() {
		return this.totalRecvMessages;
	}

	public long getTotalSendMessages() {
		return this.totalSendMessages;
	}

	public String getTxfs() {
		return txfs;
	}

	public void incRecvMessage() {
		totalRecvMessages++;
	}

	public void incSendMessage() {
		totalSendMessages++;
	}

	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}

	public IClientIO getIoHandler() {
		return this.ioHandler;
	}

	public int getWriteFirstCount() {
		return 0;
	}

	public void removeClient(IServerSideChannel client) {
		boolean found = false;
		for(JAsyncSocket sock : clients){
			if( sock.isConnected() ){
				found = true;
				break;
			}
		}
		if( !found ){
			this.totalRecvMessages = 0;
			this.totalSendMessages = 0;
		}
	}

	public void setLastReceiveTime(long lastRecv) {
		this.lastReceiveTime = lastRecv;
	}

	public void setLastSendTime(long lastSend) {
		this.lastSendTime = lastSend;
	}

	public int getIoThreadSize() {
		return ioThreadSize;
	}

	public void setIoThreadSize(int ioThreadSize) {
		this.ioThreadSize = ioThreadSize;
	}

	public String getMessageClass() {
		return messageClass;
	}

	public void setMessageClass(String messageClass) {
		this.messageClass = messageClass;
	}

	public IMessageCreator getMessageCreator() {
		return messageCreator;
	}

	public void setMessageCreator(IMessageCreator messageCreator) {
		this.messageCreator = messageCreator;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setClientSize(int clientSize) {
		this.clientSize = clientSize;
	}

	public void setBufLength(int bufLength) {
		this.bufLength = bufLength;
	}

	public void setIoHandler(IClientIO ioHandler) {
		this.ioHandler = ioHandler;
	}

	class AsyncSocketConnectThread extends Thread{
		private Selector selector;
		private long lastCheckReConnect = System.currentTimeMillis();	//���ڼ���Ƿ���Ҫ��������
		private long lastCheckTimeout = System.currentTimeMillis();
		private long now = 0;
		public AsyncSocketConnectThread(){
			super("AsyncSockConnect-"+sequence++);
		}

		public void run() {
			try{
				selector = Selector.open();
			}
			catch(Exception e){
				log.error("AsyncSocketPool �����߳��쳣��selector.open)"+e.getMessage());
				return;
			}
			long time1 = System.currentTimeMillis();
			for(JAsyncSocket client: clients){
				toConnect(client);
			}
			long milli = System.currentTimeMillis()-time1;
			log.info(getName()+",��ʼ�����첽�����¼�...[socket����ʱ��="+milli+"]");
			state = com.hzjbbis.fk.utils.State.RUNNING;
			while ( state != com.hzjbbis.fk.utils.State.STOPPING ){
				now = System.currentTimeMillis();
				//ִ��client socket channel���ӵ�������
				try{
					tryConnect();
				}
				catch(Exception e){
					log.error("AsyncSocketPool ConnectThread�쳣:"+e.getLocalizedMessage(),e);
				}
				//���socket client �Ƿ�ʱ��û��IO�� 1���Ӽ��һ�Ρ�
				if( now-lastCheckTimeout> 1000*60 ){
					checkTimeout();
					lastCheckTimeout = now;
				}
			}
			try{
				selector.close();
			}
			catch(IOException ioe){
				log.warn("selector.close�쳣��"+ioe.getLocalizedMessage());
			}
			selector = null;
			state = com.hzjbbis.fk.utils.State.STOPPED;
		}

		private void tryConnect() throws IOException{
			selector.select(100);		//ÿ��0.1�뻽��һ�Σ��Ա���client���ӳ�ʱ���
			//ʵ���ϣ�selectÿ�����N��Σ�����ʧЧ��
			if( now-lastCheckReConnect > 1000 ){
				lastCheckReConnect = now;
				for(JAsyncSocket client: clients){
					SocketChannel channel = client.getChannel();
					if( null == channel )
						toConnect(client);
					else if( channel.isConnectionPending() || channel.isConnected() )
						continue;
					else
						toConnect(client);
				}
			}
			Set<SelectionKey> set = selector.selectedKeys();
			if( set.size()>0 )
				log.debug("�������ӳɹ��¼�");
			for( SelectionKey key: set ){
				if (key.isConnectable()){
					doConnect(key);
				}
				else{
					//��Ӧ�ó����������
					log.warn("��Connectʱ��SelectionKey�Ƿ���"+key);
					key.cancel();
				}
			}
			set.clear();
		}
		
		private void doConnect(SelectionKey key){
			JAsyncSocket client = (JAsyncSocket)key.attachment();
			try{
				if( client.getChannel().finishConnect() ){
					//�ɹ�����
					ioPool.addConnectedClient(client);
					GlobalEventHandler.postEvent(new ClientConnectedEvent(AsyncSocketPool.this,client));
					if( log.isDebugEnabled() )
						log.debug("�첽���ӳɹ�:"+client);
				}
			}catch(Exception e){
				//����ʧ�ܡ�
				toConnect(client);
			}
		}
		
		private void toConnect(JAsyncSocket client){
			try{
				long now = System.currentTimeMillis();
				if( now-client.getLastConnectTime()< 15*1000 ){
					return;
				}
				client.createChannel();
				client.setLastConnectTime(now);
				SocketChannel channel = client.getChannel();
				channel.configureBlocking(false);
				channel.register(selector, SelectionKey.OP_CONNECT, client);
				if( channel.connect(new InetSocketAddress(peerIp,peerPort)) ){
					try{
						if( client.getChannel().finishConnect() ){
							//�ɹ�����
							ioPool.addConnectedClient(client);
							GlobalEventHandler.postEvent(new ClientConnectedEvent(AsyncSocketPool.this,client));
							if( log.isDebugEnabled() )
								log.debug("�첽���ӳɹ�:"+client);
						}
					}catch(Exception e){
						//����ʧ�ܡ���Ҫ�ر��� ***
						log.error(e.getLocalizedMessage(),e);
					}
				}
			}
			catch(Exception e){
				log.error(e.getLocalizedMessage(),e);
			}
		}
		
		private void checkTimeout(){
			
		}
		
	}

	public String getPeerIp() {
		return peerIp;
	}

	public void setPeerIp(String peerIp) {
		this.peerIp = peerIp;
	}

	public int getPeerPort() {
		return peerPort;
	}

	public void setPeerPort(int peerPort) {
		this.peerPort = peerPort;
	}
	
	public boolean isRunning(){
		return state.isRunning();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getModuleType() {
		return IModule.MODULE_TYPE_SOCKET_CLIENT;
	}

	public boolean isActive() {
		return false;
	}

	public String profile() {
		return "";
	}

	public String getServerAddress(){
		return "";
	}
}

