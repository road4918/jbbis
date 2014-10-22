/**
 * 异步TCP 服务器对象。 注意：关闭长时间没有任何IO的SocketChannel。
 * SocketServer包含一个IoThreadPool对象。IoThreadPool对象管理多个SocketIoThread。
 * 每个SocketChannel只能归于某个SocketIoThread执行所有操作，考虑到JDK bug，不能跨线程。
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
	//静态属性
	private static final Logger log = Logger.getLogger(TcpSocketServer.class);

	//可配置属性
	private boolean oneIpLimit = false;	//是否限制同一个IP地址只能有一个连接。对网关程序有意义

	//对象属性
	protected ServerSocketChannel ssc;
	protected Selector selector;		//用于异步Accept客户端连接
	private volatile State state = State.STOPPED; //服务器状态
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
	 * 启动TCP socket服务
	 * @return
	 */
	public boolean start(){
		if( !state.isStopped() ){
			log.warn("socket server["+port+"]非停止状态，不能启动服务。");
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
		//启动定时服务
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,60));	//定时器0，每分钟定时器
		TimerScheduler.getScheduler().addTimer(new TimerData(this,1,timeout));	//定时器1，clientTimeoutTask

		log.info("TCP服务启动成功【"+port+"】");
		GlobalEventHandler.postEvent(new ServerStartedEvent(this));
		return true;
	}
	
	public void onTimer(int timerID){
		if( 0==timerID ){	//每分钟定时器
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
			//客户端超时检查定时器
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
				log.warn("TCP服务["+name+"]超时关闭客户端连接数="+closedCount);
		}
	}
	
	/**
	 * 停止运行TCP socket服务
	 */
	public void stop(){
		if( !state.isRunning() )
			return;
		state = State.STOPPING; // stopping

		//停止Server socket侦听线程
		acceptThread.interrupt();
		int cnt = 500;
		while( acceptThread.isAlive() && cnt-->0 ){
			Thread.yield();
			try{
				acceptThread.join(20);
			}catch(InterruptedException e){}
		}
		acceptThread = null;
		
		//停止所有client socket IO 线程
		ioPool.stop();
		
		TimerScheduler.getScheduler().removeTimer(this, 0);
		TimerScheduler.getScheduler().removeTimer(this, 1);
		
		state = State.STOPPED;
		log.info("TCP服务停止【"+port+"】");
		GlobalEventHandler.postEvent(new ServerStoppedEvent(this));
	}
	
	private class AcceptThread extends Thread {
		public AcceptThread(){
			super("TcpServer-"+port+"-AcceptThread");
		}
		public void run() {
			//启动Socket服务器
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
				log.error("socketserver 侦听线程异常（selectorOpen)"+e.getMessage());
				state = com.hzjbbis.fk.utils.State.STOPPED;
				return;
			}
			state = com.hzjbbis.fk.utils.State.RUNNING;
			
			long sign = System.currentTimeMillis();
			int cnt = 0;
			int times = 0;
			//tryAccept每间隔60秒唤醒一次，提供机会检测socket client超时情况。
			while ( state != com.hzjbbis.fk.utils.State.STOPPING){
				try{
					tryAccept();
					
					//利用计数器以及时钟，检测死循环
					cnt++;
					if( cnt>= 200 ){
						long now = System.currentTimeMillis();
						if( now-sign < 1000 ){
							log.warn("server[" + port + "]Accept thread可能进入死循环。");
						}
						cnt = 0;
						sign = System.currentTimeMillis();
					}
					
					//检查socket client 是否长时间没有IO。 1分钟检查一次。
					if( times++ >= 10 ){
						checkTimeout();
					}
				}
				catch(Exception e){
					log.error("server[" + port + "]AcceptThread异常:"+e.getLocalizedMessage(),e);
				}
			}
			try{
				ssc.close();
			}
			catch(IOException ioe){
				log.warn("ssc.close异常："+ioe.getLocalizedMessage());
			}
			ssc = null;
			try{
				selector.close();
			}
			catch(IOException ioe){
				log.warn("selector.close异常："+ioe.getLocalizedMessage());
			}
			selector = null;
			log.info("server[" + port + "]listen thread is stopping");
		}
		
		private void tryAccept() throws IOException,ClosedSelectorException{
			//特别注意：JDK的selector存在缺陷。例如selector(1000)，将导致延迟1秒才检测到连接事件。
			//实际上，客户端已经连接上来。因此改为selector(50)，确保迅速检测到OP_ACCEPT
			int n = selector.select(50);
			if( n<=0 ){
				//这里不做任何事情，继续检测selectedKeys。
			}
			Set<SelectionKey> set = selector.selectedKeys();
			for( SelectionKey key: set ){
				if (key.isAcceptable()){
					try{
					doAccept();
					}catch(Exception e){
						log.warn("doAccept()异常："+e.getLocalizedMessage(),e);
						key.cancel();
					}
				}
				else{
					//不应该出现这种情况
					log.warn("在Accept时候，SelectionKey非法："+key);
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
			
			//把新Accept的SocketChannel分配到某个特定线程。
			ioPool.acceptNewClient(client);
			
			//把接受客户端连接事件加入全局事件处理器
			GlobalEventHandler.postEvent(new AcceptEvent(client));
		}
	}
	
	/**
	 * 当客户端主动断开连接时，client已经被SocketIoThread关闭。
	 * 回调本函数，从Map中清理该socketChannel。
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
	 * 从服务器端主动断开连接。应用场景：当socket client长时间没有IO操作，被服务器主动关闭。
	 * 主动关闭client对象，需要通过SocketIoThread进行具体关闭而回收资源。
	 * @param client
	 */
	public void forceCloseClient(AsyncSocketClient client){
		removeClient(client);
		client.getIoThread().closeClientRequest(client);
	}

	private void checkTimeout(){
		//注意：必须从map把对象refference拷贝出来。否则引起map冲突。
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
	 * 返回本TCP服务器的所有客户端连接对象
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

