package com.hzjbbis.fk.fe.ums;

/**
 * UMS短信通道算法:
 * 1. 必须考虑流量控制。每分钟
 */
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.simpletimer.Speedometer;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.exception.SendMessageException;
import com.hzjbbis.fk.fe.fiber.IFiber;
import com.hzjbbis.fk.fe.ums.protocol.UmsCommands;
import com.hzjbbis.fk.fe.ums.protocol.UmsField;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageLoader4Zj;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.sockclient.SimpleSocket;
import com.hzjbbis.fk.sockserver.event.MessageSendFailEvent;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;
import com.hzjbbis.fk.utils.CalendarUtil;
import com.hzjbbis.fk.utils.HexDump;
import com.hzjbbis.fk.utils.State;

/**
 * 浙江电力的UMS短信网关客户端连接模块详细定义.
 * @author bhw
 * 2008-10-20
 */
public class UmsModule extends BaseClientChannelModule implements IFiber {
	private static final Logger log = Logger.getLogger(UmsModule.class);
	//UMS短信类型
	public static final String SMS_TYPE_CH="0";
	public static final String SMS_TYPE_PDU="21";
	
	//UMS客户端可配置属性. hostIp/peerIp; hostPort/peerPort; name; txfs; 在基类定义完毕.这里不再定义.
	private String appid,apppwd;		//短信通道对应的appID和密码定义
	private String reply;				//回复地址
	private IEventHandler eventHandler;	//通过spring配置事件处理器，例如SmsMessageEventHandler
	private UmsCommands umsProtocol;
	private boolean fiber = false;

	//对象内部属性
	private State state = State.STOPPED;
	private SimpleSocket client;
	private List<MessageZj> rtuReqList = new LinkedList<MessageZj>();		//终端短信命令下行
	private List<MessageZj> genReqList = new LinkedList<MessageZj>();		//普通短信发送
	private final MessageLoader4Zj messageLoad = new MessageLoader4Zj();
	private UmsSocketThread thread;
	
	//速度控制
	private int umsSendSpeed = 100;			    //短信前置机发送速度/分钟
	private int sendUserLimit = 2;				/*给手机用户每次最多连续发送短信数*/
	private int sendRtuLimit = 10;	            /*给终端每次最多连续发送短信数*/
	private int retrieveMsgLimit = 10;			/*每次最多连续读取短信数*/
	private Speedometer speedom = new Speedometer();	//速度计,每分钟速度
	//无短信上行告警
	private long noUpLogAlertTime;		//无短信上行通讯记录告警时间(单位:毫秒)
	private List<String> simNoList;			//无短信上行通讯记录需要告警的短信号码
	private String alertContent;			//无短信上行通讯记录需要告警内容

	/**
	 * 对于短信来说，终端的通道有appid和subid来决定。每个通道只对应appid。subappid由消息本身携带。
	 */
	public String getPeerAddr() {
		return appid;
	}

	public void close() {
	}
	
	public boolean send(IMessage msg) {
		if( !sendMessage(msg) )
			throw new SendMessageException("发送消息异常");
		return true;
	}

	public boolean sendMessage(IMessage msg) {
		MessageZj zjmsg;
		if( msg.getMessageType() == MessageType.MSG_ZJ )
			zjmsg = (MessageZj)msg;
		else if( msg.getMessageType() == MessageType.MSG_GATE )
			zjmsg = ((MessageGate)msg).getInnerMessage();
		else
			zjmsg = null;
		if( null== zjmsg )
			return false;
		
		zjmsg.setTxfs(txfs);
		zjmsg.setSource(this);
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_REQ_SMS ){
			//普通短信发送
			synchronized(this.genReqList){
				this.genReqList.add(zjmsg);
			}
		}
		else{
			//终端短信发送之间判断手机号码是否有效
			ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
			String simNo=rtu.getSimNum();
			if (simNo!=null&&simNo.length()>=11&&isNumeric(simNo.trim())){
				synchronized(this.rtuReqList){
					this.rtuReqList.add(zjmsg);
				}
			}
			else{//非法手机号码创建发送失败报文返回
				if(log.isDebugEnabled())
					log.debug("rtu="+HexDump.toHex(zjmsg.head.rtua)+" simNo is error:"+simNo);
				zjmsg = zjmsg.createSendFailReply();
				this.eventHandler.handleEvent(new MessageSendFailEvent(msg,this));
			}
		}
		return true;
	}
	//判断字符串是否为数字
	public boolean isNumeric(String str){ 
	    return Pattern.matches("[0-9]*",str);
} 
	public boolean isActive() {
		return null != client && client.isAlive() ;
	}

	public boolean start() {
		lastReceiveTime = System.currentTimeMillis();
		if( !state.isStopped() )
			return false;
		state = State.STARTING;
		log.debug("ums-"+appid+"启动...");
		
		if( null == client ){
			client = new SimpleSocket(peerIp,peerPort);
		}
		if( !fiber ){
			thread = new UmsSocketThread();
			thread.start();
		}
		return true;
	}

	public void stop() {
		if( !state.isRunning() )
			return;
		state = State.STOPPING;
		client.close();
		if( !fiber ){
			thread.interrupt();
			Thread.yield();
		}
		//为线程释放资源留点时间。不需要严谨的等待线程执行完毕退出。
		try{
			Thread.sleep(100);
		}catch(Exception e){}
		
		thread = null;
		client = null;
	}

	/**
	 * 支持请求发送短信命令。主要用来给某个手机号码发送短信息。
	 * 给终端发送短信命令的方法是 doSendRtuReq
	 * @param msg : 请求发送短信浙江规约报文
	 * @return
	 */
	protected boolean doSendGenReq(MessageZj msg){
		if( msg.data.position()== msg.data.limit() )
			msg.data.position(0);
		msg.head.dlen = (short)msg.data.remaining();
		byte[] mn = new byte[14];
		msg.data.get(mn);
		int pos= 0;
		while( mn[pos]==0x00 && pos<14)
			pos++;
		if(pos>=14){
			log.warn("用户自定义短信发送失败：目标号码全0！无法发送");
			this.eventHandler.handleEvent(new MessageSendFailEvent(msg,this));
			return false;
		}
		String mobile = new String(mn,pos,14-pos);						
		byte[] ct = new byte[msg.head.dlen-14];		//华隆为GBK编码，要将整个语句的编码逆序
		msg.data.get(ct);
		//发送内容逆续
		{
			int j = ct.length-1;
			pos = 0;
			byte cc;
			while(pos<j){
				cc = ct[pos];
				ct[pos] = ct[j];
				ct[j] = cc;
				pos++; j--;
			}
		}
		String contents = new String(ct);
		
		//非法手机号码判断
		if (mobile==null||!isNumeric(mobile.trim())){
			if(log.isDebugEnabled())
				log.debug("用户自定义短信["+contents+"]发送失败,simNo is error:"+mobile);
			this.eventHandler.handleEvent(new MessageSendFailEvent(msg,this));
			return false;		
		}
		
		int ret = 0;
		StringBuffer msb=new StringBuffer(mobile);
		msb.reverse();	//手机号也是逆序

		String umsAddr = msg.getPeerAddr();
		String subappid = "";
		if( null!=umsAddr && umsAddr.length()>0 ){
			int subIndex = umsAddr.indexOf(this.appid); 
			if( subIndex>=0 ){
				subappid = umsAddr.substring( subIndex + this.appid.length() );
			}
		}
		
		String sendCont=null;			
		int maxlen=600;//短信中文格式长度扩充,由原来的60个修改300个中文

		int num=1;
		int nums=contents.length()/maxlen;
		if (contents.length()%maxlen>0){//有尾段信息的分页加1
			nums++;
		}
		String tag="";
		while(contents.length()>0){
			if (nums>1)//需要分页则增加页眉
				tag="["+num+"/"+nums+"]";
			if (contents.length()>=maxlen){
				sendCont=tag+contents.substring(0, maxlen);
				ret = umsProtocol.sendUserMessage(this.client,msb.toString(),sendCont,this.appid,subappid,this.reply); //支持主站发送hex码通信帧
				if( 0 != ret ){
					break;
				}
				contents=contents.substring(maxlen, contents.length());
			}
			else{
				if( 0 == ret ){
					//尾段信息或第一段信息
					sendCont=tag+contents.substring(0, contents.length());
					ret = umsProtocol.sendUserMessage(this.client,msb.toString(),sendCont,this.appid,subappid,this.reply); //支持主站发送hex码通信帧
				}
				contents="";
			}
			num++;
		}
		//发送返回为空才重启连接
		if( -1 == ret )
			client.close();
		
		String info = null;
		if( log.isDebugEnabled() ){
			StringBuffer sb = new StringBuffer();
			sb.append(getPeerAddr());
			if( 0 == ret )
				sb.append("成功");
			else
				sb.append("失败");
			sb.append(" ->短信通道下行报文:[mobile=");
			sb.append(mobile);
			sb.append(",contents=").append(contents).append("],subappid="+subappid);
			info = sb.toString();
			log.debug(info);
		}
		msg.setPeerAddr(getPeerAddr()+subappid);
		msg.setIoTime(System.currentTimeMillis());
		//与接收到短信消息相反，appid放在前面作为src地址。
		msg.setServerAddress( msg.getPeerAddr() + "," + mobile );
		if( 0 == ret ){
			this.eventHandler.handleEvent(new SendMessageEvent(msg,this));
		}
		else{
//			msg = msg.createSendFailReply();
			this.eventHandler.handleEvent(new MessageSendFailEvent(msg,this));
		}
		return 0 == ret;
	}
	
	protected boolean doSendRtuReq(MessageZj msgZj)
	{
		/**
		 * 如果是打包接收到终端发送的浙江规约消息，则rawData一定!=null
		 * 如果rawData==null，则一定是请求发送的浙江规约.
		 * 在发送的地方，必须实现把发送的数据打包成浙江规约原始报文，放到rawData
		 */
		msgZj.setIoTime(System.currentTimeMillis());
		
		String downReqString=null;
		
		msgZj.setPrefix(null);
		downReqString = new String(msgZj.getRawPacketString());

		int ret = -1;
		//String mobilePhone = RtuCache.getInstance().getRtu(msgZj.headOut.rtua).getCommAddress();
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(msgZj.head.rtua);
		if(rtu==null){
			log.warn("终端不存在，无法发送召测命令----"+ msgZj.head.rtua);
			return false;
		}
		String umsAddr = msgZj.getPeerAddr();
		String subappid = "";
		if( null != umsAddr && umsAddr.length()>0 ){
			int subIndex = umsAddr.indexOf(appid);
			if( subIndex >= 0 ){
				subappid = umsAddr.substring( subIndex + this.appid.length() );
			}
		}
		else{
			//根据RTU参数来选择子应用号。
			if( null != rtu.getActiveSubAppId() )
				subappid = rtu.getActiveSubAppId();
		}

		String mobilePhone = rtu.getSimNum();
		if(mobilePhone==null || mobilePhone.length()<=0){
			log.warn("终端SIM卡资料缺失,短信无法发送--"+msgZj.head.rtua);
			this.eventHandler.handleEvent(new MessageSendFailEvent(msgZj,this));
			return false;
		}else{
			ret = this.umsProtocol.sendRtuMessage(this.client,mobilePhone,downReqString,this.appid, subappid, this.reply);
			if( 0 != ret )
				this.client.close();
		}
		
		String info = null;
		if( log.isDebugEnabled() ){//if( log.isDebugEnabled() ){
			StringBuffer sb = new StringBuffer();
			sb.append(getPeerAddr());
			if( 0 == ret )
				sb.append("成功");
			else
				sb.append("失败");
			sb.append(" ->短信通道下行报文:");
			sb.append(downReqString).append(",subappid=").append(subappid);
			info = sb.toString();
			log.debug(info);			
		}

		byte msta = msgZj.head.msta;
		
		if( 0 == ret ){
			msgZj.setPeerAddr(getPeerAddr()+subappid);	
			msgZj.setIoTime(System.currentTimeMillis());
			msgZj.setServerAddress( msgZj.getPeerAddr() + "," + mobilePhone );
			this.eventHandler.handleEvent(new SendMessageEvent(msgZj,this));
		}
		else {
			/** 厂商解析模块命令 */
			if( (msta>=10 && msta<=29) || msgZj.head.rtua==0 ){
				return 0 == ret;
			}
//			msgZj = msgZj.createSendFailReply();
			this.eventHandler.handleEvent(new MessageSendFailEvent(msgZj,this));
		}
		return 0 == ret;
	}

	//属性设置
	public void setAppid(String appid) {
		this.appid = appid;
	}

	public void setApppwd(String apppwd) {
		this.apppwd = apppwd;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public void setEventHandler(IEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	/**
	 * Fiber接口函数。为了便于线程池调度，该方法不能死循环。
	 * 对于UMS来说，每次调用run，执行一批次发送和接收操作。
	 */
	public void runOnce(){
		if( state == State.STOPPING || state == State.STOPPED ){
			state = State.STOPPED;
			return;
		}
		if( state == State.STARTING )
			state = State.RUNNING;
		//1. 链路检测
		if( !isActive() ){
			long delta = System.currentTimeMillis() - client.getLastConnectTime();
			if( delta> 60* 1000 ){
				//每分钟连接一次
				boolean ret = client.reConnect();
				log.info("UMS reConnetct...");
				if( ret ){
					//1.1 登录到UMS
					if( !umsProtocol.login(client, this.appid, this.apppwd) )
						return;
				}
			}
			else{
				try{
					Thread.sleep(50);
				}catch(Exception e){}
				return;
			}
		}
		//2. 发送终端短信
		int msgCount = 0;
		while( rtuReqList.size()>0 && msgCount++ <this.sendRtuLimit && client.isAlive() ){
			if (log.isDebugEnabled())
				log.debug("发送终端短信"+msgCount+";rtuReqList="+rtuReqList.size());
			MessageZj msg;
			synchronized(rtuReqList){
				msg = rtuReqList.remove(0);
			}
			doSendRtuReq(msg);
			this.totalSendMessages++;
			this.lastSendTime = System.currentTimeMillis();
			speedom.add(1);
			if ( speedom.getSpeed1()> umsSendSpeed ){	//控制短信收发速度
				try{
					Thread.sleep(50);
				}catch(Exception e){}
				break;
			}
		}
		
		//3. 巡测终端上行短信
		msgCount = 0;
		while( msgCount <this.retrieveMsgLimit && client.isAlive() ){
			if (log.isDebugEnabled())
				log.debug("巡测终端上行短信:"+msgCount);
			Map<String,String> repMap = umsProtocol.retrieveSMS(client,this.appid);
			if( null == repMap ){
				//没有短信
				break;
			}
			msgCount++;
			//3.1 依据应答，生成上行浙江规约。
			String rawMessage = repMap.get(UmsField.FIELD_CONTENT);
			String strDate = repMap.get(UmsField.FIELD_RECVDATE);		//实际收到短信时间
			String strTime = repMap.get(UmsField.FIELD_RECVTIME);		//实际收到短信时间
			String from = repMap.get(UmsField.FIELD_FROM);				//发送方手机号码
			if( null== from )
				from = " ";
			String receiver = repMap.get(UmsField.FIELD_RECEIVER);		//例如：95598301301
			if( null == receiver )
				receiver = " ";
			
			lastReceiveTime = System.currentTimeMillis();
			this.totalRecvMessages++;
			try{
				MessageZj msg = messageLoad.loadMessage(rawMessage);
				if (log.isDebugEnabled())
					log.debug(msgCount+"--retrieveSMS msg:"+msg);
				if( null != msg ){
					msg.setPeerAddr(getPeerAddr());
					msg.setIoTime(System.currentTimeMillis());
					msg.setSource(UmsModule.this);					
					msg.setTxfs(txfs);
					msg.setServerAddress(from+","+receiver);
					UmsModule.this.eventHandler.handleEvent(new ReceiveMessageEvent(msg,UmsModule.this));
				}
				else{
					//可能是运营商通过终端上行的问候消息。
					log.info("非浙江规约短信上行："+rawMessage+",from="+from+",datetime="+strDate+strTime);
				}
			}
			catch(Exception exp){
				log.error("MessageZj.loadMessage 异常,原因="+exp.getLocalizedMessage()+",rawpacket="+rawMessage);
			}
			speedom.add(1);
			if ( speedom.getSpeed1()> umsSendSpeed ){	//控制短信收发速度
				try{
					Thread.sleep(50);
				}catch(Exception e){}
				break;
			}
		}
		
		//4. 给普通用户发送短信
		msgCount = 0;
		while( this.genReqList.size()>0 && msgCount++ <this.sendUserLimit && client.isAlive() ){
			if (log.isDebugEnabled())
				log.debug("给普通用户发送短信"+msgCount+";genReqList="+genReqList.size());
			MessageZj msg;
			synchronized(genReqList){
				msg = genReqList.remove(0);
			}
			doSendGenReq(msg);
			this.lastSendTime = System.currentTimeMillis();
			this.totalSendMessages++;
			speedom.add(1);
			if ( speedom.getSpeed1()> umsSendSpeed ){	//控制短信收发速度
				try{
					Thread.sleep(50);
				}catch(Exception e){}
				break;
			}
		}
		
		//5. 检测是否长时间没有短信上行。
		if(client.isAlive() && noUpLogAlertTime>0 && null != alertContent){
			long delt=System.currentTimeMillis()- lastReceiveTime;
			if( delt> noUpLogAlertTime ){//指定时间内无消息上报，发送信息给指定SIM卡号//by yangjie 2008/03/10
				if (simNoList!=null){
					for( String mobileNo: simNoList){
						umsProtocol.sendUserMessage(this.client, mobileNo, alertContent,this.appid,null,this.reply);
					}
				}
				client.close();							
				lastReceiveTime = System.currentTimeMillis();
				log.info("UMS通讯应用ID"+appid+"的通道在指定时间范围内无消息上报,重启链路");
			}
		}
	}

	public void setNoUpLogAlertTime(long noUpLogAlertTime) {
		this.noUpLogAlertTime = noUpLogAlertTime;
	}

	public void setSimNoList(List<String> simNoList) {
		this.simNoList = simNoList;
	}

	public void setAlertContent(String alertContent) {
		this.alertContent = alertContent;
	}

	public final void setUmsProtocol(UmsCommands umsProtocol) {
		this.umsProtocol = umsProtocol;
	}

	public final void setUmsSendSpeed(int umsSendSpeed) {
		this.umsSendSpeed = umsSendSpeed;
	}

	public final void setSendUserLimit(int sendUserLimit) {
		this.sendUserLimit = sendUserLimit;
	}

	public final void setSendRtuLimit(int sendRtuLimit) {
		this.sendRtuLimit = sendRtuLimit;
	}
	
	public void setFiber( boolean isFiber ){
		fiber = isFiber;
	}
	
	public boolean isFiber(){
		return fiber;
	}
	
	private class UmsSocketThread extends Thread {
		public UmsSocketThread(){
			super("ums.thread."+appid);
		}
		
		@Override
		public void run() {
			while( state != com.hzjbbis.fk.utils.State.STOPPED ){
				try{
					UmsModule.this.runOnce();
				}
				catch(Exception exp){
					log.error("UMS通信处理异常："+exp.getLocalizedMessage(),exp);
				}
			}
		}
	}

	public final void setRetrieveMsgLimit(int retrieveMsgLimit) {
		this.retrieveMsgLimit = retrieveMsgLimit;
	}

	@Override
	public String profile() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\r\n<sockclient-profile type=\"").append(getModuleType()).append("\">");
		sb.append("\r\n    ").append("<name>").append(getName()).append("</name>");
		sb.append("\r\n    ").append("<ip>").append(this.getPeerIp()).append("</ip>");
		sb.append("\r\n    ").append("<port>").append(this.getPeerPort()).append("</port>");
		sb.append("\r\n    ").append("<state>").append(isActive()).append("</state>");

		sb.append("\r\n    ").append("<txfs>").append(txfs).append("</txfs>");
		sb.append("\r\n    ").append("<totalRecv>").append(totalRecvMessages).append("</totalRecv>");
		sb.append("\r\n    ").append("<totalSend>").append(totalSendMessages).append("</totalSend>");
		sb.append("\r\n    ").append("<speed>").append(speedom.getSpeed1()).append("</speed>");

		String stime = CalendarUtil.getTimeString(lastReceiveTime);
		sb.append("\r\n    ").append("<lastRecv>").append(stime).append("</lastRecv>");
		stime = CalendarUtil.getTimeString(lastSendTime);
		sb.append("\r\n    ").append("<lastSend>").append(stime).append("</lastSend>");
		sb.append("\r\n</sockclient-profile>");
		return sb.toString();
	}

	@Override
	public String getModuleType() {
		return IModule.MODULE_TYPE_UMS_CLIENT;
	}

}
