/**
 * TCP客户端连接模块。主要用于业务处理器->通信前置机；通信前置机->gprs网关等；
 * 每个连接作为一个可监控模块。
 * 通信前置机，包含N个网关连接Module，以及N个短信连接Module。
 * 多个网关clientModule，上行消息一致处理，因此需要考虑特殊的事件处理泵。
 */
package com.hzjbbis.fk.clientmod;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.simpletimer.ITimerFunctor;
import com.hzjbbis.fk.common.simpletimer.TimerData;
import com.hzjbbis.fk.common.simpletimer.TimerScheduler;
import com.hzjbbis.fk.common.spi.IClientModule;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.IMessageCreator;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.gate.MessageGateCreator;
import com.hzjbbis.fk.sockclient.JSocket;
import com.hzjbbis.fk.sockclient.JSocketListener;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;
import com.hzjbbis.fk.tracelog.TraceLog;
import com.hzjbbis.fk.utils.CalendarUtil;

/**
 * @author bhw
 * 2008-10-10
 */
public class ClientModule implements JSocketListener, IClientModule, ITimerFunctor {
	private static final Logger log = Logger.getLogger(ClientModule.class);
	//可配置属性
	private String name = "GPRS网关客户端";
	private String moduleType = IModule.MODULE_TYPE_SOCKET_CLIENT;
	private String hostIp="127.0.0.1";
	private int hostPort = 10001;
	private int bufLength = 256;		//默认缓冲区长度
	private IMessageCreator messageCreator = new MessageGateCreator();
	private int timeout = 2;			//读或者写超时，单位秒
	private String txfs = "02";			//通信方式配置。
	private IEventHandler eventHandler;	//通过spring配置事件处理器，例如GateMessageEventHandler
	private JSocket socket = null;
	//心跳管理
	private int heartInterval = 0;		//心跳间隔(秒)。如果间隔时间内无上行报文，则发送心跳。0表示取消心跳。
	private int requestNum = 200;		//客户端向服务器请求接收报文数量。-1表示请求服务器永远异步推送报文
	private long lastHeartbeat = System.currentTimeMillis();	//上次心跳应答时间

	//统计属性,初始化自动为0
	private long lastReceiveTime = System.currentTimeMillis();							//最新接收报文时间
	private long lastSendTime = 0;							//最近发送成功的时间
	private long totalRecvMessages=0,totalSendMessages=0;	//总共收、发消息总数
	private int msgRecvPerMinute=0,msgSendPerMinute=0;		//每分钟收、发报文个数
	private Object statisticsRecv = new Object() ,statisticsSend = new Object();

	//内部属性
	private boolean active = false;
	private IMessage heartMsg = null;		//定时请求报文。以心跳的形式。
	private int curRecv = 200;				//对收到服务器报文计数，收到requestNum报文后，发送请求。

	public boolean sendMessage(IMessage msg){
		if( ! active )
			return false;
		boolean result = socket.sendMessage(msg);
		if(result && msg.isHeartbeat() ){
			TraceLog _tracer = TraceLog.getTracer(socket.getClass());
			if( _tracer.isEnabled() )
				_tracer.trace("send heart-beat ok.");
		}
		
		return result;
	}
	
	public void onClose(JSocket client) {
		active = false;
	}

	public void onConnected(JSocket client) {
		if( heartInterval>0 && requestNum>0 ){
			sendMessage(heartMsg);
			if( log.isDebugEnabled() ){
				log.debug("连接时，请求报文数量="+requestNum);
			}
			curRecv = requestNum;
		}
		active = true;
	}

	public void onReceive(JSocket client, IMessage msg) {
		synchronized(statisticsRecv){
			msgRecvPerMinute++;
			totalRecvMessages++;
			if( requestNum > 0 ){
				if( msg.isHeartbeat() ){
					lastHeartbeat = System.currentTimeMillis();
//					TraceLog _tracer = TraceLog.getTracer(socket.getClass());
//					if( _tracer.isEnabled() )
//						_tracer.trace("receive heart-beat,lastHeartbeat="+lastHeartbeat);
				}

				if( --curRecv == 0 ){
					sendMessage(heartMsg);
					if( log.isDebugEnabled() )
						log.debug("onReceive时，server传输数量达到requestNum，重新请求报文数量="+requestNum);
					curRecv = requestNum;
				}
				else{
					if( log.isDebugEnabled() ){
						IMessage amsg = msg;
						if( msg.getMessageType() == MessageType.MSG_GATE ){
							amsg = ((MessageGate)msg).getInnerMessage();
							if( null != amsg )
								log.debug("剩余报文数量="+curRecv+",msg="+amsg);
						}
						else
							log.debug("剩余报文数量="+curRecv+",msg="+amsg);
					}
				}
			}
		}
		lastReceiveTime = System.currentTimeMillis();
		try{
			eventHandler.handleEvent(new ReceiveMessageEvent(msg,client));
		}catch(Exception e){}
	}

	public void onSend(JSocket client, IMessage msg) {
		synchronized(statisticsSend){
			msgSendPerMinute++;
			totalSendMessages++;
		}
		lastSendTime = System.currentTimeMillis();
		try{
			eventHandler.handleEvent(new SendMessageEvent(msg,client));
		}catch(Exception e){}
	}

	public String getModuleType() {
		return this.moduleType;
	}
	
	public void setModuleType(String modType){
		this.moduleType = modType;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String nm){
		name = nm;
	}

	public String getTxfs() {
		return txfs;
	}

	public boolean isActive() {
		return active;
	}

	public void init(){
		if( null == socket ){
			//如果没有通过spring配置client对象
			socket = new JSocket();
			socket.setHostIp(hostIp);
			socket.setHostPort(hostPort);
		}
	}
	
	public boolean start() {
		if( heartInterval>0 && null == heartMsg ){
			heartMsg = messageCreator.createHeartBeat(requestNum);
			curRecv = requestNum;
		}
		init();
		socket.setBufLength(bufLength);
		socket.setMessageCreator(messageCreator);
		socket.setTimeout(timeout);
		socket.setListener(this);
		socket.setTxfs(txfs);
		socket.init();
		//启动定时服务
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,60));	//定时器0，每分钟定时器
		//心跳间隔.检测心跳发送情况
		if( heartInterval>0 ){
			TimerScheduler.getScheduler().addTimer(new TimerData(this,1,heartInterval/1000));	//定时器1
		}
		lastHeartbeat = System.currentTimeMillis();
		return true;
	}

	public void stop() {
		TimerScheduler.getScheduler().removeTimer(this, 0);
		if( null != socket )
			socket.close();
	}

	public String profile() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\r\n    <sockclient-profile type=\"").append(getModuleType()).append("\">");
		sb.append("\r\n        ").append("<name>").append(getName()).append("</name>");
		sb.append("\r\n        ").append("<port>").append(hostIp+":"+hostPort).append("</port>");
		sb.append("\r\n        ").append("<state>").append(isActive()).append("</state>");
		sb.append("\r\n        ").append("<timeout>").append(timeout).append("</timeout>");

		sb.append("\r\n        ").append("<txfs>").append(txfs).append("</txfs>");
		sb.append("\r\n        ").append("<totalRecv>").append(totalRecvMessages).append("</totalRecv>");
		sb.append("\r\n        ").append("<totalSend>").append(totalSendMessages).append("</totalSend>");
		sb.append("\r\n        ").append("<perMinuteRecv>").append(msgRecvPerMinute).append("</perMinuteRecv>");
		sb.append("\r\n        ").append("<perMinuteSend>").append(msgSendPerMinute).append("</perMinuteSend>");

		String stime = CalendarUtil.getTimeString(lastReceiveTime);
		sb.append("\r\n        ").append("<lastRecv>").append(stime).append("</lastRecv>");
		stime = CalendarUtil.getTimeString(lastSendTime);
		sb.append("\r\n        ").append("<lastSend>").append(stime).append("</lastSend>");
		sb.append("\r\n    </sockclient-profile>");
		return sb.toString();
	}
	
	public void onTimer(int id){
		//先检测心跳定时
		if( 1 == id && heartInterval>0 ){
			long interval = System.currentTimeMillis() - this.lastHeartbeat ;
			if( interval>= heartInterval ){
				curRecv = requestNum;
				sendMessage(heartMsg);
			}
			if( interval> (heartInterval<<1) ){
				TraceLog _trace = TraceLog.getTracer(socket.getClass());
				if( _trace.isEnabled() )
					_trace.trace("no heartbeat reply within 2 heartbeat intervals，connection will be reset. client="+socket.getPeerAddr()+",heartInterval="+heartInterval+",interval="+interval+",lastHeartbeat="+lastHeartbeat);
				socket.reConnect();
				//Attention, last heart-beat time must update when socket is reconnected every time. 
				this.lastHeartbeat = System.currentTimeMillis();
			}
		}
		else if( 0 ==  id ){
			//每分钟定时器
//			GlobalEventHandler.postEvent(new ModuleProfileEvent(this));
			synchronized(statisticsRecv){
				msgRecvPerMinute = 0;
			}
			synchronized(statisticsSend){
				msgSendPerMinute = 0;
			}
		}
	}

	public long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public long getLastSendTime() {
		return lastSendTime;
	}

	public int getMsgRecvPerMinute() {
		return this.msgRecvPerMinute;
	}

	public int getMsgSendPerMinute() {
		return this.msgSendPerMinute;
	}

	public long getTotalRecvMessages() {
		return this.totalRecvMessages;
	}

	public long getTotalSendMessages() {
		return this.totalSendMessages;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public int getHostPort() {
		return hostPort;
	}

	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}

	public int getBufLength() {
		return bufLength;
	}

	public void setBufLength(int bufLength) {
		this.bufLength = bufLength;
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

	public JSocket getSocket() {
		return socket;
	}

	public void setSocket(JSocket socket) {
		this.socket = socket;
	}

	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}

	public void setEventHandler(IEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void setHeartInterval(int heartInterval) {
		this.heartInterval = heartInterval*1000;
	}

	public int getRequestNum() {
		return requestNum;
	}

	public void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}

	@Override
	public String toString() {
		return profile();
	}

}
