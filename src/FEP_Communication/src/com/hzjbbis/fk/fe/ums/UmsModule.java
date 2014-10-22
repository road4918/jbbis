package com.hzjbbis.fk.fe.ums;

/**
 * UMS����ͨ���㷨:
 * 1. ���뿼���������ơ�ÿ����
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
 * �㽭������UMS�������ؿͻ�������ģ����ϸ����.
 * @author bhw
 * 2008-10-20
 */
public class UmsModule extends BaseClientChannelModule implements IFiber {
	private static final Logger log = Logger.getLogger(UmsModule.class);
	//UMS��������
	public static final String SMS_TYPE_CH="0";
	public static final String SMS_TYPE_PDU="21";
	
	//UMS�ͻ��˿���������. hostIp/peerIp; hostPort/peerPort; name; txfs; �ڻ��ඨ�����.���ﲻ�ٶ���.
	private String appid,apppwd;		//����ͨ����Ӧ��appID�����붨��
	private String reply;				//�ظ���ַ
	private IEventHandler eventHandler;	//ͨ��spring�����¼�������������SmsMessageEventHandler
	private UmsCommands umsProtocol;
	private boolean fiber = false;

	//�����ڲ�����
	private State state = State.STOPPED;
	private SimpleSocket client;
	private List<MessageZj> rtuReqList = new LinkedList<MessageZj>();		//�ն˶�����������
	private List<MessageZj> genReqList = new LinkedList<MessageZj>();		//��ͨ���ŷ���
	private final MessageLoader4Zj messageLoad = new MessageLoader4Zj();
	private UmsSocketThread thread;
	
	//�ٶȿ���
	private int umsSendSpeed = 100;			    //����ǰ�û������ٶ�/����
	private int sendUserLimit = 2;				/*���ֻ��û�ÿ������������Ͷ�����*/
	private int sendRtuLimit = 10;	            /*���ն�ÿ������������Ͷ�����*/
	private int retrieveMsgLimit = 10;			/*ÿ�����������ȡ������*/
	private Speedometer speedom = new Speedometer();	//�ٶȼ�,ÿ�����ٶ�
	//�޶������и澯
	private long noUpLogAlertTime;		//�޶�������ͨѶ��¼�澯ʱ��(��λ:����)
	private List<String> simNoList;			//�޶�������ͨѶ��¼��Ҫ�澯�Ķ��ź���
	private String alertContent;			//�޶�������ͨѶ��¼��Ҫ�澯����

	/**
	 * ���ڶ�����˵���ն˵�ͨ����appid��subid��������ÿ��ͨ��ֻ��Ӧappid��subappid����Ϣ����Я����
	 */
	public String getPeerAddr() {
		return appid;
	}

	public void close() {
	}
	
	public boolean send(IMessage msg) {
		if( !sendMessage(msg) )
			throw new SendMessageException("������Ϣ�쳣");
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
			//��ͨ���ŷ���
			synchronized(this.genReqList){
				this.genReqList.add(zjmsg);
			}
		}
		else{
			//�ն˶��ŷ���֮���ж��ֻ������Ƿ���Ч
			ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
			String simNo=rtu.getSimNum();
			if (simNo!=null&&simNo.length()>=11&&isNumeric(simNo.trim())){
				synchronized(this.rtuReqList){
					this.rtuReqList.add(zjmsg);
				}
			}
			else{//�Ƿ��ֻ����봴������ʧ�ܱ��ķ���
				if(log.isDebugEnabled())
					log.debug("rtu="+HexDump.toHex(zjmsg.head.rtua)+" simNo is error:"+simNo);
				zjmsg = zjmsg.createSendFailReply();
				this.eventHandler.handleEvent(new MessageSendFailEvent(msg,this));
			}
		}
		return true;
	}
	//�ж��ַ����Ƿ�Ϊ����
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
		log.debug("ums-"+appid+"����...");
		
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
		//Ϊ�߳��ͷ���Դ����ʱ�䡣����Ҫ�Ͻ��ĵȴ��߳�ִ������˳���
		try{
			Thread.sleep(100);
		}catch(Exception e){}
		
		thread = null;
		client = null;
	}

	/**
	 * ֧�������Ͷ��������Ҫ������ĳ���ֻ����뷢�Ͷ���Ϣ��
	 * ���ն˷��Ͷ�������ķ����� doSendRtuReq
	 * @param msg : �����Ͷ����㽭��Լ����
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
			log.warn("�û��Զ�����ŷ���ʧ�ܣ�Ŀ�����ȫ0���޷�����");
			this.eventHandler.handleEvent(new MessageSendFailEvent(msg,this));
			return false;
		}
		String mobile = new String(mn,pos,14-pos);						
		byte[] ct = new byte[msg.head.dlen-14];		//��¡ΪGBK���룬Ҫ���������ı�������
		msg.data.get(ct);
		//������������
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
		
		//�Ƿ��ֻ������ж�
		if (mobile==null||!isNumeric(mobile.trim())){
			if(log.isDebugEnabled())
				log.debug("�û��Զ������["+contents+"]����ʧ��,simNo is error:"+mobile);
			this.eventHandler.handleEvent(new MessageSendFailEvent(msg,this));
			return false;		
		}
		
		int ret = 0;
		StringBuffer msb=new StringBuffer(mobile);
		msb.reverse();	//�ֻ���Ҳ������

		String umsAddr = msg.getPeerAddr();
		String subappid = "";
		if( null!=umsAddr && umsAddr.length()>0 ){
			int subIndex = umsAddr.indexOf(this.appid); 
			if( subIndex>=0 ){
				subappid = umsAddr.substring( subIndex + this.appid.length() );
			}
		}
		
		String sendCont=null;			
		int maxlen=600;//�������ĸ�ʽ��������,��ԭ����60���޸�300������

		int num=1;
		int nums=contents.length()/maxlen;
		if (contents.length()%maxlen>0){//��β����Ϣ�ķ�ҳ��1
			nums++;
		}
		String tag="";
		while(contents.length()>0){
			if (nums>1)//��Ҫ��ҳ������ҳü
				tag="["+num+"/"+nums+"]";
			if (contents.length()>=maxlen){
				sendCont=tag+contents.substring(0, maxlen);
				ret = umsProtocol.sendUserMessage(this.client,msb.toString(),sendCont,this.appid,subappid,this.reply); //֧����վ����hex��ͨ��֡
				if( 0 != ret ){
					break;
				}
				contents=contents.substring(maxlen, contents.length());
			}
			else{
				if( 0 == ret ){
					//β����Ϣ���һ����Ϣ
					sendCont=tag+contents.substring(0, contents.length());
					ret = umsProtocol.sendUserMessage(this.client,msb.toString(),sendCont,this.appid,subappid,this.reply); //֧����վ����hex��ͨ��֡
				}
				contents="";
			}
			num++;
		}
		//���ͷ���Ϊ�ղ���������
		if( -1 == ret )
			client.close();
		
		String info = null;
		if( log.isDebugEnabled() ){
			StringBuffer sb = new StringBuffer();
			sb.append(getPeerAddr());
			if( 0 == ret )
				sb.append("�ɹ�");
			else
				sb.append("ʧ��");
			sb.append(" ->����ͨ�����б���:[mobile=");
			sb.append(mobile);
			sb.append(",contents=").append(contents).append("],subappid="+subappid);
			info = sb.toString();
			log.debug(info);
		}
		msg.setPeerAddr(getPeerAddr()+subappid);
		msg.setIoTime(System.currentTimeMillis());
		//����յ�������Ϣ�෴��appid����ǰ����Ϊsrc��ַ��
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
		 * ����Ǵ�����յ��ն˷��͵��㽭��Լ��Ϣ����rawDataһ��!=null
		 * ���rawData==null����һ���������͵��㽭��Լ.
		 * �ڷ��͵ĵط�������ʵ�ְѷ��͵����ݴ�����㽭��Լԭʼ���ģ��ŵ�rawData
		 */
		msgZj.setIoTime(System.currentTimeMillis());
		
		String downReqString=null;
		
		msgZj.setPrefix(null);
		downReqString = new String(msgZj.getRawPacketString());

		int ret = -1;
		//String mobilePhone = RtuCache.getInstance().getRtu(msgZj.headOut.rtua).getCommAddress();
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(msgZj.head.rtua);
		if(rtu==null){
			log.warn("�ն˲����ڣ��޷������ٲ�����----"+ msgZj.head.rtua);
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
			//����RTU������ѡ����Ӧ�úš�
			if( null != rtu.getActiveSubAppId() )
				subappid = rtu.getActiveSubAppId();
		}

		String mobilePhone = rtu.getSimNum();
		if(mobilePhone==null || mobilePhone.length()<=0){
			log.warn("�ն�SIM������ȱʧ,�����޷�����--"+msgZj.head.rtua);
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
				sb.append("�ɹ�");
			else
				sb.append("ʧ��");
			sb.append(" ->����ͨ�����б���:");
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
			/** ���̽���ģ������ */
			if( (msta>=10 && msta<=29) || msgZj.head.rtua==0 ){
				return 0 == ret;
			}
//			msgZj = msgZj.createSendFailReply();
			this.eventHandler.handleEvent(new MessageSendFailEvent(msgZj,this));
		}
		return 0 == ret;
	}

	//��������
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
	 * Fiber�ӿں�����Ϊ�˱����̳߳ص��ȣ��÷���������ѭ����
	 * ����UMS��˵��ÿ�ε���run��ִ��һ���η��ͺͽ��ղ�����
	 */
	public void runOnce(){
		if( state == State.STOPPING || state == State.STOPPED ){
			state = State.STOPPED;
			return;
		}
		if( state == State.STARTING )
			state = State.RUNNING;
		//1. ��·���
		if( !isActive() ){
			long delta = System.currentTimeMillis() - client.getLastConnectTime();
			if( delta> 60* 1000 ){
				//ÿ��������һ��
				boolean ret = client.reConnect();
				log.info("UMS reConnetct...");
				if( ret ){
					//1.1 ��¼��UMS
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
		//2. �����ն˶���
		int msgCount = 0;
		while( rtuReqList.size()>0 && msgCount++ <this.sendRtuLimit && client.isAlive() ){
			if (log.isDebugEnabled())
				log.debug("�����ն˶���"+msgCount+";rtuReqList="+rtuReqList.size());
			MessageZj msg;
			synchronized(rtuReqList){
				msg = rtuReqList.remove(0);
			}
			doSendRtuReq(msg);
			this.totalSendMessages++;
			this.lastSendTime = System.currentTimeMillis();
			speedom.add(1);
			if ( speedom.getSpeed1()> umsSendSpeed ){	//���ƶ����շ��ٶ�
				try{
					Thread.sleep(50);
				}catch(Exception e){}
				break;
			}
		}
		
		//3. Ѳ���ն����ж���
		msgCount = 0;
		while( msgCount <this.retrieveMsgLimit && client.isAlive() ){
			if (log.isDebugEnabled())
				log.debug("Ѳ���ն����ж���:"+msgCount);
			Map<String,String> repMap = umsProtocol.retrieveSMS(client,this.appid);
			if( null == repMap ){
				//û�ж���
				break;
			}
			msgCount++;
			//3.1 ����Ӧ�����������㽭��Լ��
			String rawMessage = repMap.get(UmsField.FIELD_CONTENT);
			String strDate = repMap.get(UmsField.FIELD_RECVDATE);		//ʵ���յ�����ʱ��
			String strTime = repMap.get(UmsField.FIELD_RECVTIME);		//ʵ���յ�����ʱ��
			String from = repMap.get(UmsField.FIELD_FROM);				//���ͷ��ֻ�����
			if( null== from )
				from = " ";
			String receiver = repMap.get(UmsField.FIELD_RECEIVER);		//���磺95598301301
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
					//��������Ӫ��ͨ���ն����е��ʺ���Ϣ��
					log.info("���㽭��Լ�������У�"+rawMessage+",from="+from+",datetime="+strDate+strTime);
				}
			}
			catch(Exception exp){
				log.error("MessageZj.loadMessage �쳣,ԭ��="+exp.getLocalizedMessage()+",rawpacket="+rawMessage);
			}
			speedom.add(1);
			if ( speedom.getSpeed1()> umsSendSpeed ){	//���ƶ����շ��ٶ�
				try{
					Thread.sleep(50);
				}catch(Exception e){}
				break;
			}
		}
		
		//4. ����ͨ�û����Ͷ���
		msgCount = 0;
		while( this.genReqList.size()>0 && msgCount++ <this.sendUserLimit && client.isAlive() ){
			if (log.isDebugEnabled())
				log.debug("����ͨ�û����Ͷ���"+msgCount+";genReqList="+genReqList.size());
			MessageZj msg;
			synchronized(genReqList){
				msg = genReqList.remove(0);
			}
			doSendGenReq(msg);
			this.lastSendTime = System.currentTimeMillis();
			this.totalSendMessages++;
			speedom.add(1);
			if ( speedom.getSpeed1()> umsSendSpeed ){	//���ƶ����շ��ٶ�
				try{
					Thread.sleep(50);
				}catch(Exception e){}
				break;
			}
		}
		
		//5. ����Ƿ�ʱ��û�ж������С�
		if(client.isAlive() && noUpLogAlertTime>0 && null != alertContent){
			long delt=System.currentTimeMillis()- lastReceiveTime;
			if( delt> noUpLogAlertTime ){//ָ��ʱ��������Ϣ�ϱ���������Ϣ��ָ��SIM����//by yangjie 2008/03/10
				if (simNoList!=null){
					for( String mobileNo: simNoList){
						umsProtocol.sendUserMessage(this.client, mobileNo, alertContent,this.appid,null,this.reply);
					}
				}
				client.close();							
				lastReceiveTime = System.currentTimeMillis();
				log.info("UMSͨѶӦ��ID"+appid+"��ͨ����ָ��ʱ�䷶Χ������Ϣ�ϱ�,������·");
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
					log.error("UMSͨ�Ŵ����쳣��"+exp.getLocalizedMessage(),exp);
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