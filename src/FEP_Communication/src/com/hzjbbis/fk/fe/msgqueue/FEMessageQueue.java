/**
 * 通信前置机的报文队列。
 * 报文队列的生产者（插入）：GateMessageEventHandle，UmsMessageEventHandle
 * 报文队列的消费者（取走）：BpServerEventHandle
 * 所有终端下行，都通过通信前置机的消息队列，统一下行，以便进行通道管理。
 * 
 * 异步上行到业务处理器的算法：
 * (1)   BP－>ReqNum到FE－>FE从队列取msg－>BP；
 * 		如果队列为空，则等到队列消息;
 * (2)   (2.1)FE从队列取消息成功发送给BP－>(2.2.1)ReqNum允许继续发送->(2.3)从队列取继续发送
 * 		 (2.2.2) 等待新的ReqNum；
 * 
 * 难点：上行BP(业务处理器)发送需要等待2件事情：ReqNumber和上行消息.
 * 对于多业务处理器情况：
 * 按照地市代码，动态分配到BP client连接对象。2.2版本提供，实现类：MessageDispatch2Bp
 */
package com.hzjbbis.fk.fe.msgqueue;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.queue.CacheQueue;
import com.hzjbbis.fk.common.simpletimer.ITimerFunctor;
import com.hzjbbis.fk.common.simpletimer.TimerData;
import com.hzjbbis.fk.common.simpletimer.TimerScheduler;
import com.hzjbbis.fk.common.spi.IMessageQueue;
import com.hzjbbis.fk.common.spi.IProfile;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.fe.ChannelManage;
import com.hzjbbis.fk.fe.userdefine.UserDefineMessageQueue;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.GateHead;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.zj.MessageZjCreator;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 * revised: 2008-11-13 21:37
 * (1)Master station initiate to send heart beat packet to RTU. 
 */
public class FEMessageQueue implements IMessageQueue,ITimerFunctor, IProfile{
	private static final Logger log = Logger.getLogger(FEMessageQueue.class);
	private CacheQueue cacheQueue;		//spring 配置实现。
	private int rtuHeartbeatInterval = 15*60;	//15分钟心跳间隔。
	//在漏点补召完成之前，需要缓存任务报文。王卫峰提出的功能。 2008－12－24
	private CacheQueue taskCacheQueue;
	//内部属性
	private final int heartbeatTimer = 0;
	private TimerData td = null;
	//辅助属性，以便提高系统性能
	private long hbInterval = rtuHeartbeatInterval * 1000;
	private MessageZjCreator messageCreator = new MessageZjCreator();
	private boolean dispatchRandom = true;
	private boolean noConvert = false;		//是否直接上行浙江规约原始报文.
	
	//当系统退出时候，需要把队列数据写到缓存文件。
	private Runnable shutdownHook = new Runnable(){
		public void run(){
			FEMessageQueue.this.dispose();
		}
	};

	public void setCacheQueue( CacheQueue queue ){
		cacheQueue = queue;
		if( null == td )
			initialize();
		FasSystem.getFasSystem().addShutdownHook(shutdownHook);
	}
	
	public void setRtuHeartbeatInterval(int interval){
		rtuHeartbeatInterval = interval;
		hbInterval = rtuHeartbeatInterval * 1000; 
		initialize();
	}

	public void initialize(){
		if( null != td ){
			TimerScheduler.getScheduler().removeTimer(this, heartbeatTimer);
			td = null;
		}
		if( this.rtuHeartbeatInterval > 10 ){
			td = new TimerData(this,heartbeatTimer,this.rtuHeartbeatInterval);
			TimerScheduler.getScheduler().addTimer(td);
		}
	}

	public void onTimer(final int timerID ){
		if( timerID == heartbeatTimer ){
			for( ComRtu rtu: RtuManage.getInstance().getAllComRtu() ){
				//只有上行过GPRS报文的终端，才进行主站主动心跳尝试。
				if( null == rtu.getActiveGprs() )
					continue;
				long distance = Math.abs(System.currentTimeMillis() - rtu.getLastIoTime());
				if( distance > hbInterval ){
					//超过间隔时间没有收到上行报文
					//主站（通信前置机）主动向终端发起心跳检测。
					MessageZj heartbeat = messageCreator.createHeartBeat(rtu.getRtua());
					rtu.setLastIoTime(System.currentTimeMillis());
					sendMessage(heartbeat);
				}
			}
		}
	}
	
	//消息队列统一管理终端下行消息
	public boolean sendMessage(IMessage msg){
		if( msg.getMessageType() == MessageType.MSG_ZJ ){
			MessageZj zjmsg = (MessageZj)msg;
			IChannel channel = null;
			boolean result = false;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
				//如果是主站主动发起心跳，不走短信。
				channel = ChannelManage.getInstance().getGPRSChannel(zjmsg.head.rtua);
				if( null != channel )
					result = channel.send(zjmsg);
			}
			else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
				result = UserDefineMessageQueue.getInstance().sendMessageDown(zjmsg);
			}
			else{
				channel = ChannelManage.getInstance().getChannel(zjmsg.head.rtua);
				if( null == channel ){
					log.warn("该终端无可用通道下行,RTUA="+HexDump.toHex(zjmsg.head.rtua));
					return false;
				}
				result = channel.send(zjmsg);
			}
			return result;
		}
		else if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate gatemsg = (MessageGate)msg;
			//如果指定短信通道
			String appstring = gatemsg.getHead().getAttributeAsString(GateHead.ATT_DESTADDR);
			if( null != appstring && appstring.length()>=9 ){
				String appid = appstring.substring(5, 9);
				IChannel channel = ChannelManage.getInstance().getChannel(appid);
				if( null == channel ){
					log.warn("指定短信应用号无对应通道：appid="+appid);
					handleSendFail(gatemsg.getInnerMessage());
					return false;
				}
				MessageZj zjmsg = gatemsg.getInnerMessage();
				zjmsg.setPeerAddr(appstring);	//例如：955983401 95598340101
				return channel.send(zjmsg);
			}
			//普通浙江规约下行报文
			MessageZj zjmsg = gatemsg.getInnerMessage();
			if( null == zjmsg ){
				log.error("下行的网关消息没有包含浙江规约帧。gatemsg="+gatemsg.getRawPacketString());
				return false;
			}
			//检测是否短信发送请求。
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_REQ_SMS ){
				IChannel umsChannel = ChannelManage.getInstance().getActiveUmsChannel();
				if( null == umsChannel ){
					log.warn("当前没有在线的UMS短信通道，请求发送短信失败。");
					return false;
				}
				return umsChannel.send(zjmsg);
			}
			/**
			 * 针对主站下行请求，需要考虑UDP不在线情况。
			 */
			ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
			if( null == rtu ){
				log.warn("终端下行失败，终端不在缓存中,rtua="+HexDump.toHex(zjmsg.head.rtua));
				return false;
			}			
			IChannel channel = ChannelManage.getInstance().getChannel(zjmsg.head.rtua);
			if( zjmsg.head.msta != 0 ){
				//主站请求
				rtu.setLastReqTime(System.currentTimeMillis());
			}
			if( null == channel ){
				log.warn("该终端无可用通道下行,RTUA="+HexDump.toHex(zjmsg.head.rtua));
				handleSendFail(zjmsg);
				return false;
			}
			//如果是GPRS下行，peerAddr为业务处理器IP地址。
			//下行目标地址需要设置。借助status属性，serverAddress属性将被发送到网关，不能用.
			//对于UMS下行，目标地址为appid，不需要关心status属性。
			zjmsg.setStatus(rtu.getActiveGprs());
			return channel.send(msg);
		}
		log.error("FEMessageQueue只支持MessageGate,MessageZj消息下行。程序错啦！");
		return false;
	}
	
	private void handleSendFail(MessageZj zjmsg ){
		try{
			IChannel channel = (IChannel)zjmsg.getSource();
			MessageZj repSendFail = zjmsg.createSendFailReply();
			MessageGate gatemsg = new MessageGate();
			gatemsg.setUpInnerMessage(repSendFail);
			channel.send(gatemsg);
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	
	/**
	 * 对于指定短信下行情况。
	 * @param msg
	 * @return
	 */
	public boolean sendMessageByUms(IMessage msg){
		if( msg instanceof MessageZj ){
			MessageZj zjmsg = (MessageZj)msg;
			IChannel channel = ChannelManage.getInstance().getUmsChannel(null,zjmsg.head.rtua);
			if( null == channel ){
				//
				log.warn("该终端无可用短信通道,RTUA="+HexDump.toHex(zjmsg.head.rtua));
				return false;
			}
			channel.send(msg);
			return true;
		}
		else if( msg instanceof MessageGate ){
			MessageGate gatemsg = (MessageGate)msg;
			MessageZj zjmsg = gatemsg.getInnerMessage();
			if( null == zjmsg ){
				log.error("下行的网关消息没有包含浙江规约帧。gatemsg="+gatemsg.getRawPacketString());
				return false;
			}
			//从网关消息中提取APPID，如果指定下行短信应用号和子应用号。
			String appid = zjmsg.getPeerAddr();
			IChannel channel = ChannelManage.getInstance().getUmsChannel(appid,zjmsg.head.rtua);
			if( null == channel ){
				//
				log.warn("该终端无可用通道下行,RTUA="+HexDump.toHex(zjmsg.head.rtua));
				return false;
			}
			channel.send(msg);
			return true;
		}
		log.error("FEMessageQueue只支持MessageGate,MessageZj消息下行。程序错啦！");
		return false;
	}
	
	//下面定义消息队列的方法
	public IMessage take(){
		return cacheQueue.take();
	}
	
	public IMessage poll(){
		return cacheQueue.poll();
	}
	
	/**
	 * 当通信前置机收到网关上行报文时，调用此函数，把上行消息放入队列，
	 * 以便发送给业务处理器。
	 * @param msg
	 */
	public void offer(IMessage msg0){
		if( msg0.getMessageType() == MessageType.MSG_GATE ){
			RuntimeException re = new RuntimeException();
			log.warn("出现插入gate 消息",re);
			return;
		}
		MessageZj zjmsg = (MessageZj)msg0;
		//必须先放到cacheQueue，然后取出来进行发送选择。
		cacheQueue.offer(zjmsg);
		if( null != taskCacheQueue ){
			try{
				if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK )
					taskCacheQueue.offer(zjmsg);
			}catch(Exception e){
				
			}
		}
		//按照优先级取需要发送的报文
		zjmsg = (MessageZj)cacheQueue.poll();
		//可能被别的线程取走，需要判断是否为null
		if( null == zjmsg )
			return;
		
		IServerSideChannel bpChannel = null;
		//检测是否有bpclient允许发送报文。
		if( this.dispatchRandom ){
			//往业务处理器随机发送机制。
			bpChannel = MessageDispatch2Bp.getInstance().getIdleChannel();
		}
		else{
			//按照地市码往业务处理器发送
			bpChannel = MessageDispatch2Bp.getInstance().getBpChannel(zjmsg.head.rtua_a1);
		}
		if( null == bpChannel ){
			pushBack(zjmsg);
			return;
		}
		boolean success = false;
		if( noConvert )
			success = bpChannel.send(zjmsg);
		else{
			//把浙江规约报文转换成网关规约，发送给前置机。
			MessageGate gateMsg = new MessageGate();
			gateMsg.setUpInnerMessage(zjmsg);
			success = bpChannel.send(gateMsg);
		}
		if( !success ){
			pushBack(zjmsg);
		}
	}
	
	/**
	 * 当消息从client的发送队列回收时，调用putback。
	 * @param msg
	 */
	public void pushBack(IMessage msg){
		cacheQueue.offer(msg);
	}
	
	public int size(){
		return cacheQueue.size();
	}

	@Override
	public String toString() {
		return "FEMessageQueue";
	}

	public CacheQueue getTaskCacheQueue() {
		return taskCacheQueue;
	}

	public void setTaskCacheQueue(CacheQueue taskCacheQueue) {
		this.taskCacheQueue = taskCacheQueue;
	}
	
	public void onBpClientConnected(IServerSideChannel bpClient){
		MessageDispatch2Bp.getInstance().onBpClientConnected(bpClient);
	}
	
	public void onBpClientClosed(IServerSideChannel bpClient){
		MessageDispatch2Bp.getInstance().onBpClientClosed(bpClient);
	}
	
	public void setDispatchRandom(boolean dispatchRandom){
		this.dispatchRandom = dispatchRandom;
	}
	
	public void setNoConvert(boolean noConvert) {
		this.noConvert = noConvert;
	}

	public String profile() {
		StringBuffer sb = new StringBuffer(256);
		sb.append("\r\n    <message-queue type=\"fe\">");
		sb.append("\r\n        <size>").append(size()).append("</size>");
		sb.append("\r\n    </message-queue>");
		return sb.toString();
	}
	
	public void dispose(){
		cacheQueue.dispose();
	}
}
