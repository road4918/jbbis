/**
 * ͨ��ǰ�û��ı��Ķ��С�
 * ���Ķ��е������ߣ����룩��GateMessageEventHandle��UmsMessageEventHandle
 * ���Ķ��е������ߣ�ȡ�ߣ���BpServerEventHandle
 * �����ն����У���ͨ��ͨ��ǰ�û�����Ϣ���У�ͳһ���У��Ա����ͨ������
 * 
 * �첽���е�ҵ���������㷨��
 * (1)   BP��>ReqNum��FE��>FE�Ӷ���ȡmsg��>BP��
 * 		�������Ϊ�գ���ȵ�������Ϣ;
 * (2)   (2.1)FE�Ӷ���ȡ��Ϣ�ɹ����͸�BP��>(2.2.1)ReqNum�����������->(2.3)�Ӷ���ȡ��������
 * 		 (2.2.2) �ȴ��µ�ReqNum��
 * 
 * �ѵ㣺����BP(ҵ������)������Ҫ�ȴ�2�����飺ReqNumber��������Ϣ.
 * ���ڶ�ҵ�����������
 * ���յ��д��룬��̬���䵽BP client���Ӷ���2.2�汾�ṩ��ʵ���ࣺMessageDispatch2Bp
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
	private CacheQueue cacheQueue;		//spring ����ʵ�֡�
	private int rtuHeartbeatInterval = 15*60;	//15�������������
	//��©�㲹�����֮ǰ����Ҫ���������ġ�����������Ĺ��ܡ� 2008��12��24
	private CacheQueue taskCacheQueue;
	//�ڲ�����
	private final int heartbeatTimer = 0;
	private TimerData td = null;
	//�������ԣ��Ա����ϵͳ����
	private long hbInterval = rtuHeartbeatInterval * 1000;
	private MessageZjCreator messageCreator = new MessageZjCreator();
	private boolean dispatchRandom = true;
	private boolean noConvert = false;		//�Ƿ�ֱ�������㽭��Լԭʼ����.
	
	//��ϵͳ�˳�ʱ����Ҫ�Ѷ�������д�������ļ���
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
				//ֻ�����й�GPRS���ĵ��նˣ��Ž�����վ�����������ԡ�
				if( null == rtu.getActiveGprs() )
					continue;
				long distance = Math.abs(System.currentTimeMillis() - rtu.getLastIoTime());
				if( distance > hbInterval ){
					//�������ʱ��û���յ����б���
					//��վ��ͨ��ǰ�û����������ն˷���������⡣
					MessageZj heartbeat = messageCreator.createHeartBeat(rtu.getRtua());
					rtu.setLastIoTime(System.currentTimeMillis());
					sendMessage(heartbeat);
				}
			}
		}
	}
	
	//��Ϣ����ͳһ�����ն�������Ϣ
	public boolean sendMessage(IMessage msg){
		if( msg.getMessageType() == MessageType.MSG_ZJ ){
			MessageZj zjmsg = (MessageZj)msg;
			IChannel channel = null;
			boolean result = false;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
				//�������վ�����������������߶��š�
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
					log.warn("���ն��޿���ͨ������,RTUA="+HexDump.toHex(zjmsg.head.rtua));
					return false;
				}
				result = channel.send(zjmsg);
			}
			return result;
		}
		else if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate gatemsg = (MessageGate)msg;
			//���ָ������ͨ��
			String appstring = gatemsg.getHead().getAttributeAsString(GateHead.ATT_DESTADDR);
			if( null != appstring && appstring.length()>=9 ){
				String appid = appstring.substring(5, 9);
				IChannel channel = ChannelManage.getInstance().getChannel(appid);
				if( null == channel ){
					log.warn("ָ������Ӧ�ú��޶�Ӧͨ����appid="+appid);
					handleSendFail(gatemsg.getInnerMessage());
					return false;
				}
				MessageZj zjmsg = gatemsg.getInnerMessage();
				zjmsg.setPeerAddr(appstring);	//���磺955983401 95598340101
				return channel.send(zjmsg);
			}
			//��ͨ�㽭��Լ���б���
			MessageZj zjmsg = gatemsg.getInnerMessage();
			if( null == zjmsg ){
				log.error("���е�������Ϣû�а����㽭��Լ֡��gatemsg="+gatemsg.getRawPacketString());
				return false;
			}
			//����Ƿ���ŷ�������
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_REQ_SMS ){
				IChannel umsChannel = ChannelManage.getInstance().getActiveUmsChannel();
				if( null == umsChannel ){
					log.warn("��ǰû�����ߵ�UMS����ͨ���������Ͷ���ʧ�ܡ�");
					return false;
				}
				return umsChannel.send(zjmsg);
			}
			/**
			 * �����վ����������Ҫ����UDP�����������
			 */
			ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
			if( null == rtu ){
				log.warn("�ն�����ʧ�ܣ��ն˲��ڻ�����,rtua="+HexDump.toHex(zjmsg.head.rtua));
				return false;
			}			
			IChannel channel = ChannelManage.getInstance().getChannel(zjmsg.head.rtua);
			if( zjmsg.head.msta != 0 ){
				//��վ����
				rtu.setLastReqTime(System.currentTimeMillis());
			}
			if( null == channel ){
				log.warn("���ն��޿���ͨ������,RTUA="+HexDump.toHex(zjmsg.head.rtua));
				handleSendFail(zjmsg);
				return false;
			}
			//�����GPRS���У�peerAddrΪҵ������IP��ַ��
			//����Ŀ���ַ��Ҫ���á�����status���ԣ�serverAddress���Խ������͵����أ�������.
			//����UMS���У�Ŀ���ַΪappid������Ҫ����status���ԡ�
			zjmsg.setStatus(rtu.getActiveGprs());
			return channel.send(msg);
		}
		log.error("FEMessageQueueֻ֧��MessageGate,MessageZj��Ϣ���С����������");
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
	 * ����ָ���������������
	 * @param msg
	 * @return
	 */
	public boolean sendMessageByUms(IMessage msg){
		if( msg instanceof MessageZj ){
			MessageZj zjmsg = (MessageZj)msg;
			IChannel channel = ChannelManage.getInstance().getUmsChannel(null,zjmsg.head.rtua);
			if( null == channel ){
				//
				log.warn("���ն��޿��ö���ͨ��,RTUA="+HexDump.toHex(zjmsg.head.rtua));
				return false;
			}
			channel.send(msg);
			return true;
		}
		else if( msg instanceof MessageGate ){
			MessageGate gatemsg = (MessageGate)msg;
			MessageZj zjmsg = gatemsg.getInnerMessage();
			if( null == zjmsg ){
				log.error("���е�������Ϣû�а����㽭��Լ֡��gatemsg="+gatemsg.getRawPacketString());
				return false;
			}
			//��������Ϣ����ȡAPPID�����ָ�����ж���Ӧ�úź���Ӧ�úš�
			String appid = zjmsg.getPeerAddr();
			IChannel channel = ChannelManage.getInstance().getUmsChannel(appid,zjmsg.head.rtua);
			if( null == channel ){
				//
				log.warn("���ն��޿���ͨ������,RTUA="+HexDump.toHex(zjmsg.head.rtua));
				return false;
			}
			channel.send(msg);
			return true;
		}
		log.error("FEMessageQueueֻ֧��MessageGate,MessageZj��Ϣ���С����������");
		return false;
	}
	
	//���涨����Ϣ���еķ���
	public IMessage take(){
		return cacheQueue.take();
	}
	
	public IMessage poll(){
		return cacheQueue.poll();
	}
	
	/**
	 * ��ͨ��ǰ�û��յ��������б���ʱ�����ô˺�������������Ϣ������У�
	 * �Ա㷢�͸�ҵ��������
	 * @param msg
	 */
	public void offer(IMessage msg0){
		if( msg0.getMessageType() == MessageType.MSG_GATE ){
			RuntimeException re = new RuntimeException();
			log.warn("���ֲ���gate ��Ϣ",re);
			return;
		}
		MessageZj zjmsg = (MessageZj)msg0;
		//�����ȷŵ�cacheQueue��Ȼ��ȡ�������з���ѡ��
		cacheQueue.offer(zjmsg);
		if( null != taskCacheQueue ){
			try{
				if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK )
					taskCacheQueue.offer(zjmsg);
			}catch(Exception e){
				
			}
		}
		//�������ȼ�ȡ��Ҫ���͵ı���
		zjmsg = (MessageZj)cacheQueue.poll();
		//���ܱ�����߳�ȡ�ߣ���Ҫ�ж��Ƿ�Ϊnull
		if( null == zjmsg )
			return;
		
		IServerSideChannel bpChannel = null;
		//����Ƿ���bpclient�����ͱ��ġ�
		if( this.dispatchRandom ){
			//��ҵ������������ͻ��ơ�
			bpChannel = MessageDispatch2Bp.getInstance().getIdleChannel();
		}
		else{
			//���յ�������ҵ����������
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
			//���㽭��Լ����ת�������ع�Լ�����͸�ǰ�û���
			MessageGate gateMsg = new MessageGate();
			gateMsg.setUpInnerMessage(zjmsg);
			success = bpChannel.send(gateMsg);
		}
		if( !success ){
			pushBack(zjmsg);
		}
	}
	
	/**
	 * ����Ϣ��client�ķ��Ͷ��л���ʱ������putback��
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
