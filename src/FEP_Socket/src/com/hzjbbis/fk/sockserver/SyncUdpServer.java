/**
 * ��UDP������
 */
package com.hzjbbis.fk.sockserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.simpletimer.ITimerFunctor;
import com.hzjbbis.fk.common.simpletimer.TimerData;
import com.hzjbbis.fk.common.simpletimer.TimerScheduler;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseSocketServer;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.sockserver.event.ModuleProfileEvent;
import com.hzjbbis.fk.utils.HexDump;
import com.hzjbbis.fk.utils.State;

/**
 * @author bhw 
 *
 */
public class SyncUdpServer extends BaseSocketServer implements IModule,ITimerFunctor{
	private static final Logger log = Logger.getLogger(SyncUdpServer.class);

	//�����ò���
	private boolean oneIpLimit = false;	//�Ƿ�����ͬһ��IP��ַֻ����һ�����ӡ������س���������

	//����״̬����
	private volatile State state = State.STOPPED; //������״̬
	private DatagramChannel dgc;
	private Object channelLock = new Object();		//DatagramChannelͬ����
	private boolean channelReady = false;
	private UdpIoThread ioThread;
	private ByteBuffer readBuffer = null, writeBuffer=null;
	
	//��������
	private final HashMap<String,UdpClient> clients = new HashMap<String,UdpClient>(5120); 

	public SyncUdpServer(){
	}

	public String getModuleType() {
		return IModule.MODULE_TYPE_SOCKET_SERVER;
	}

	public boolean isActive() {
		return state.isActive();
	}

	/**
	 * UDP��ʽͬ������message
	 * @param msg
	 * @param sa
	 * @return
	 */
	public boolean send(IMessage msg,UdpClient client){
		synchronized(channelLock){
			try{
				SocketAddress sa = client.getSocketAddress();
				writeBuffer.clear();
				msg.write(writeBuffer);
				writeBuffer.flip();
				this.dgc.send(writeBuffer, sa);
				client.setLastIoTime();
				return true;
			}catch(Exception e){
				log.error("UDP["+port+"]���ͱ����쳣:"+e.getLocalizedMessage(),e);
			}
		}
		return false;
	}
	
	public boolean start() {
		if ( state.isActive() )
			return false;
		state = State.STARTING;
		readBuffer = ByteBuffer.allocateDirect(bufLength);
		writeBuffer = ByteBuffer.allocateDirect(bufLength);
		
		//������ʱ����
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,60));	//��ʱ��0��ÿ���Ӷ�ʱ��
		TimerScheduler.getScheduler().addTimer(new TimerData(this,1,timeout));	//��ʱ��1��clientTimeoutTask

		//��UdpIoThread�д�ͨ��: openChannel();
		try{
			ioThread = new UdpIoThread();
			ioThread.start();
			while( state != State.RUNNING )
			{
				Thread.yield();
				Thread.sleep(10);
			}
		} catch (Exception exp) {
			log.fatal(exp.getLocalizedMessage());
			return false;
		}
		if( log.isInfoEnabled() )
			log.info("UDP����"+port+ "���Ѿ�����!");
		return true;
	}
	
	public void onTimer(int timerID ){
		if( 0 == timerID ){
			long now = System.currentTimeMillis();
			if( now-lastReceiveTime < 60*1000 || now-lastSendTime < 60*1000 )
				GlobalEventHandler.postEvent(new ModuleProfileEvent(SyncUdpServer.this));
			msgRecvPerMinute = 0;	msgSendPerMinute = 0;
		}
		else if( 1== timerID ){
			ArrayList<UdpClient>list = new ArrayList<UdpClient>(clients.values());
			long now = System.currentTimeMillis();
			for(UdpClient client: list){
				if( now-client.getLastReadTime() > timeout*1000 ){
					synchronized(clients){
						String clientKey = client.getPeerAddr();
						if( oneIpLimit )
							clientKey = client.getPeerIp();
						clients.remove(clientKey);
					}
				}
			}
		}
	}
	
	private boolean openChannel(){
		synchronized(channelLock){
			try {
				dgc = DatagramChannel.open();
				dgc.socket().setReceiveBufferSize(bufLength);
				dgc.socket().setSendBufferSize(bufLength);
				dgc.socket().setReuseAddress(true);
				dgc.configureBlocking(true);
				InetSocketAddress addr = null;
				if( null == ip )
					addr = new InetSocketAddress(port);
				else
					addr = new InetSocketAddress(ip,port);
				dgc.socket().bind(addr);
			} catch (Exception exp) {
				log.fatal("UDP����������ʧ��["+port+"]",exp);
				channelReady = false;
			}
			channelReady = true;
			return channelReady;
		}
	}
	
	private void closeChannel(){
		synchronized(channelLock){
			try {
				if(null != dgc )
					dgc.close();
			} catch (IOException e) {
				log.warn(e.getMessage());
			} finally {
				dgc = null;
			}
			channelReady = false;
		}
	}
	
	public void stop() {
		state = State.STOPPING; // stopping
		if( null != ioThread ){
			ioThread.interrupt();
			try {
				if( ioThread.isAlive() )
				ioThread.join(200);
			} catch (InterruptedException e) {	}
			ioThread = null;
		}
		
		readBuffer = null;	writeBuffer = null;

		TimerScheduler.getScheduler().removeTimer(this, 0);
		TimerScheduler.getScheduler().removeTimer(this, 1);

		if( log.isInfoEnabled() )
			log.info("UDP������[" + port + "] ֹͣ���У�");
		state = State.STOPPED;
	}

	private class UdpIoThread extends Thread {
		public UdpIoThread() {
			super("UDP["+port+"]-IO-Thread");
		}

		public void run() {
			if( log.isDebugEnabled() )
				log.debug(this.getName()+"����...");
			state = com.hzjbbis.fk.utils.State.RUNNING;
			while ( state != com.hzjbbis.fk.utils.State.STOPPING) //if state is not stopping
			{
				//���ͨ���Ƿ�����
				if( !channelReady ){
					if( !openChannel() ){
						//��ͨ��ʧ��
						closeChannel();
						try{
						Thread.sleep(60*1000);
						}catch(Exception exp){}
						continue;
					}
				}
				try{
					drainChannel();
				}
				catch( ClosedByInterruptException exp){
					log.error("UDP������["+port+"]���������쳣:"+exp.getLocalizedMessage(),exp);
					Thread.interrupted();
					closeChannel();
					continue;
				}
				catch( AsynchronousCloseException exp ){
					log.error("UDP������["+port+"]���������쳣:"+exp.getLocalizedMessage(),exp);
					Thread.interrupted();
					closeChannel();
					continue;
				}
				catch(ClosedChannelException exp){
					log.error("UDP������["+port+"]���������쳣:"+exp.getLocalizedMessage(),exp);
					Thread.interrupted();
					closeChannel();
					continue;
				}catch(Exception exp){
					log.error("UDP������["+port+"]���������쳣:"+exp.getLocalizedMessage(),exp);
				}
			}
			closeChannel();
			state = com.hzjbbis.fk.utils.State.STOPPED;
		}
		
		void drainChannel() throws Exception {
			SocketAddress sa;
			readBuffer.clear();
			sa = dgc.receive(readBuffer);
			if( null == sa ){
				log.error("UDP���������������쳣��dgc.receive(readBuffer)����NULL");
				return;
			}
			if (readBuffer.position() == 0)
				return;
			
			//�ѻ���������Ϣд��ĳ����Щ����Ϣ��
			readBuffer.flip();
			
			/**
			 * ���ڵ����նˣ�ÿ�ͻ���ֻ����һ��UDP���ӵ���������
			 * ����ϣ����뿼��ÿ���ͻ��˻��������UDP���������ݵ������
			 */
			String peerip = ((InetSocketAddress)sa).getAddress().getHostAddress();
			int port = ((InetSocketAddress)sa).getPort();
			String key = peerip;
			if( !oneIpLimit )
				key += ":"+port;
			UdpClient client = clients.get(key);
			if( null == client ){
				client = new UdpClient(sa,SyncUdpServer.this);
				synchronized(clients){
					clients.put(key, client);
				}
			}
			if(log.isDebugEnabled()){
				log.debug("UDP-"+port+"-�յ�["+sa.toString()+"]���б���:"+HexDump.hexDump(readBuffer));
			}
			//��ȫ�ֻ���������ת�Ƶ�client���󻺳�����
			if( client.getBufRead().remaining()< readBuffer.remaining() )
				client.getBufRead().clear();
			client.getBufRead().put(readBuffer);
			client.getBufRead().flip();
			client.setLastReadTime();
			ioHandler.onReceive(client);
		}
	}	// end of UdpIoThread

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("S-UDP[").append(port).append("]");
		return sb.toString();
	}

	public boolean isOneIpLimit() {
		return oneIpLimit;
	}

	public void setOneIpLimit(boolean oneIpLimit) {
		this.oneIpLimit = oneIpLimit;
	}

	public int getClientSize(){
		synchronized(clients){
			return clients.size();
		}
	}
	
	/**
	 * ���ر�UDP�����������пͻ��˶���
	 */
	public IServerSideChannel[] getClients(){
		synchronized(clients){
			return clients.values().toArray(new IServerSideChannel[clients.size()]);
		}
	}

	public void removeClient(IServerSideChannel client) {
		synchronized(clients){
			String clientKey = client.getPeerAddr();
			if( oneIpLimit )
				clientKey = client.getPeerIp();
			clients.remove(clientKey);
		}
		super.removeClient(client);
	}
}
