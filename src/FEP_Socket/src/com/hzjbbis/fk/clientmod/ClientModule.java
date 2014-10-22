/**
 * TCP�ͻ�������ģ�顣��Ҫ����ҵ������->ͨ��ǰ�û���ͨ��ǰ�û�->gprs���صȣ�
 * ÿ��������Ϊһ���ɼ��ģ�顣
 * ͨ��ǰ�û�������N����������Module���Լ�N����������Module��
 * �������clientModule��������Ϣһ�´��������Ҫ����������¼�����á�
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
	//����������
	private String name = "GPRS���ؿͻ���";
	private String moduleType = IModule.MODULE_TYPE_SOCKET_CLIENT;
	private String hostIp="127.0.0.1";
	private int hostPort = 10001;
	private int bufLength = 256;		//Ĭ�ϻ���������
	private IMessageCreator messageCreator = new MessageGateCreator();
	private int timeout = 2;			//������д��ʱ����λ��
	private String txfs = "02";			//ͨ�ŷ�ʽ���á�
	private IEventHandler eventHandler;	//ͨ��spring�����¼�������������GateMessageEventHandler
	private JSocket socket = null;
	//��������
	private int heartInterval = 0;		//�������(��)��������ʱ���������б��ģ�����������0��ʾȡ��������
	private int requestNum = 200;		//�ͻ����������������ձ���������-1��ʾ�����������Զ�첽���ͱ���
	private long lastHeartbeat = System.currentTimeMillis();	//�ϴ�����Ӧ��ʱ��

	//ͳ������,��ʼ���Զ�Ϊ0
	private long lastReceiveTime = System.currentTimeMillis();							//���½��ձ���ʱ��
	private long lastSendTime = 0;							//������ͳɹ���ʱ��
	private long totalRecvMessages=0,totalSendMessages=0;	//�ܹ��ա�����Ϣ����
	private int msgRecvPerMinute=0,msgSendPerMinute=0;		//ÿ�����ա������ĸ���
	private Object statisticsRecv = new Object() ,statisticsSend = new Object();

	//�ڲ�����
	private boolean active = false;
	private IMessage heartMsg = null;		//��ʱ�����ġ�����������ʽ��
	private int curRecv = 200;				//���յ����������ļ������յ�requestNum���ĺ󣬷�������

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
				log.debug("����ʱ������������="+requestNum);
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
						log.debug("onReceiveʱ��server���������ﵽrequestNum����������������="+requestNum);
					curRecv = requestNum;
				}
				else{
					if( log.isDebugEnabled() ){
						IMessage amsg = msg;
						if( msg.getMessageType() == MessageType.MSG_GATE ){
							amsg = ((MessageGate)msg).getInnerMessage();
							if( null != amsg )
								log.debug("ʣ�౨������="+curRecv+",msg="+amsg);
						}
						else
							log.debug("ʣ�౨������="+curRecv+",msg="+amsg);
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
			//���û��ͨ��spring����client����
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
		//������ʱ����
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,60));	//��ʱ��0��ÿ���Ӷ�ʱ��
		//�������.��������������
		if( heartInterval>0 ){
			TimerScheduler.getScheduler().addTimer(new TimerData(this,1,heartInterval/1000));	//��ʱ��1
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
		//�ȼ��������ʱ
		if( 1 == id && heartInterval>0 ){
			long interval = System.currentTimeMillis() - this.lastHeartbeat ;
			if( interval>= heartInterval ){
				curRecv = requestNum;
				sendMessage(heartMsg);
			}
			if( interval> (heartInterval<<1) ){
				TraceLog _trace = TraceLog.getTracer(socket.getClass());
				if( _trace.isEnabled() )
					_trace.trace("no heartbeat reply within 2 heartbeat intervals��connection will be reset. client="+socket.getPeerAddr()+",heartInterval="+heartInterval+",interval="+interval+",lastHeartbeat="+lastHeartbeat);
				socket.reConnect();
				//Attention, last heart-beat time must update when socket is reconnected every time. 
				this.lastHeartbeat = System.currentTimeMillis();
			}
		}
		else if( 0 ==  id ){
			//ÿ���Ӷ�ʱ��
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
