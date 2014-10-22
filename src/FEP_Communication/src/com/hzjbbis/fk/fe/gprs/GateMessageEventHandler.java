/**
 * ����������ͨ��ǰ�û�֮�䱨���շ��¼�����
 * ���б��Ľ������ȼ����У��Ա�ҵ����������
 * ע�⴦������ͳ�ơ������ȡ�
 */
package com.hzjbbis.fk.fe.gprs;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.fe.filecache.HeartbeatPersist;
import com.hzjbbis.fk.fe.filecache.RtuParamsCache;
import com.hzjbbis.fk.fe.msgqueue.FEMessageQueue;
import com.hzjbbis.fk.fe.userdefine.UserDefineMessageQueue;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;
import com.hzjbbis.fk.utils.HexDump;
/**
 * @author bhw
 *
 */
public class GateMessageEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(GateMessageEventHandler.class);
//	private static HeartBeatMessage bate = new HeartBeatMessage();
	private FEMessageQueue msgQueue;	//spring ����ʵ�֡�
	private UserDefineMessageQueue udefQueue;	//spring ����ʵ�֡����ҽ���ģ��������Ϣ����
	private AsyncService asyncDbService;		//������������ԭʼ����
	
//	private HeartBeatMessage heartBeat;//spring ����ʵ�֣���������
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}
	/**
	 * �յ�GPRS���ص����б��ġ�
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){
				//�ͻ�������ı���������Ӧ��
				log.info(mgate);
				return;		//�����������
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY ){
				MessageZj zjmsg = mgate.getInnerMessage();
				_handleZjMessage(zjmsg,e);
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_SENDFAIL ){
				//GPRS��������ʧ�ܣ���Ҫ������ͨ������ͨ�����е��նˡ�
				MessageZj zjmsg = mgate.getInnerMessage();
				//�粻�߶���:�����Զ��屨��
				if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE 
						|| zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART )
					return;

				//GPRS����ʧ�ܣ���Ҫת���š��ƶ��������в���.
				if( null != zjmsg && log.isDebugEnabled() )
					log.debug("��������ʧ�ܱ���,ת����ͨ��:"+zjmsg);
				msgQueue.sendMessageByUms(zjmsg);
				return;
			}
			else if( mgate.getHead().getCommand() == MessageGate.REP_MONITOR_RELAY_PROFILE ){
				String gateProfile = new String(mgate.getData().array());
				FasSystem.getFasSystem().addGprsGateProfile(e.getClient().getPeerAddr(), gateProfile);
				return;
			}
			else {
				//������������
				log.error("�����������");
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			_handleZjMessage((MessageZj)msg,e);
		}
	}
	
	private void _handleZjMessage(MessageZj zjmsg,ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("�������б���:"+zjmsg);
		/** ͨ��ǰ�û��յ��������б��ģ���Ҫ���⴦��
		 *  ��1���������Ĵ�MySQL��
		 *  ��2���ն˹���������
		 *  ��3�������ն�����������IP��port������վ���ò�һ�´���
		 */
		//1. ���ն˶���������ҵ��ն˶���
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
		if( null == rtu ){
//			UndocRtuMsgCache.addMessage(zjmsg);
			String strRtua = HexDump.toHex(zjmsg.head.rtua);
			log.warn("�ն˲��ڻ����У���Ҫ���¼��ء�rtua=" + strRtua );
			//�����ն�����£���Ҫ����һ������
			rtu = new ComRtu();
			rtu.setLogicAddress(strRtua);
			rtu.setRtua(zjmsg.head.rtua);
//			rtu.setDeptCode("0");
			RtuManage.getInstance().putComRtuToCache(rtu);
		}
		
		//��Ҫ�����ݿ����
		
		//2. ���¹���
		rtu.setLastGprsTime(System.currentTimeMillis());
		rtu.setLastIoTime(rtu.getLastGprsTime());
		//�ն�GPRS��ַ
		String gprsIpAddr = zjmsg.getPeerAddr();
		if( null != gprsIpAddr && gprsIpAddr.length()>0 )
			rtu.setRtuIpAddr(gprsIpAddr);
		
		//3. ����ͳ�ơ������Զ��ظ������Գ���*2,��¼�ظ�
		int flow = zjmsg.length();
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ||
				zjmsg.head.c_func == MessageConst.ZJ_FUNC_LOGOUT ){
			rtu.addDownGprsFlowmeter(flow);	rtu.addUpGprsFlowmeter(flow);
			rtu.incUpGprsCount(); rtu.incDownGprsCount();
		}
		else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_LOGIN ){
			rtu.addUpGprsFlowmeter(flow); rtu.addDownGprsFlowmeter(flow-3);
			rtu.incUpGprsCount(); rtu.incDownGprsCount();
		}
		else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK ){
			rtu.incTaskCount();
			rtu.addUpGprsFlowmeter(flow);
			rtu.incUpGprsCount();
		}
		else{
			rtu.addUpGprsFlowmeter(flow);
			rtu.incUpGprsCount();
		}
		
		//4. �ն˹������� ��ϵ����
		try{
			String gateAddr = event.getClient().getPeerAddr();	//���ص�ǰ�û��ӿڵ�ַ��
			if( ! gateAddr.equals(rtu.getActiveGprs()) ){
				rtu.setActiveGprs(gateAddr);
				//�ն������ӵ����ط����������仯����Ҫ���ٸ��»����ļ������߶��ڸ������ݿ�
				//��ʼ��ʱ����Ҫ�����ݿ���ء�
				RtuParamsCache.getInstance().addRtu(rtu);		//�ն˲�������
				
				//��Ҫ���GPRS��ַ��һ�µ������
				String serverAddr = zjmsg.getServerAddress();
				if( null != serverAddr && null != rtu.getCommAddress() ){
					if( "02".equals(rtu.getCommType())){
						if( ! serverAddr.equals(rtu.getCommAddress()) ){
							rtu.setMisGprsAddress(serverAddr);
							log.warn("�ն�ʵ�����е�ַ���ʲ���һ�£�rtua="+HexDump.toHex(zjmsg.head.rtua)+",serverAddress="+serverAddr);
						}
					}
				}
			}
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}
		
		//5.1 ԭʼ���ı���
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
		//5.2 �������ı���
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
			//������������ҵ���¼�
			HeartbeatPersist.getInstance().handleHeartbeat(rtu.getRtua());
//			heartBeat.putBeat(rtu.getLogicAddress(),zjmsg.getIoTime(),rtu.getDeptCode());
			return;
		}
		
		//6. �����Զ��屨�ģ���Ҫֱ�ӷ��͸����ҡ����ܰ���Ŀǰ������ȡ���ģʽ��
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE )
			udefQueue.offer(zjmsg);
		else
			msgQueue.offer(zjmsg);
	}
	
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		MessageZj zjmsg;
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				//�ͻ�������ı���������Ӧ��
				return;
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				zjmsg = mgate.getInnerMessage();
				zjmsg.setTxfs(mgate.getTxfs());
				zjmsg.setIoTime(mgate.getIoTime());
				zjmsg.setSource(mgate.getSource());
			}
			else
				return;
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			zjmsg = (MessageZj)msg;
		}
		else
			return;
		//ͳ������
		//1. ���ն˶���������ҵ��ն˶���
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
		if( null == rtu )
			return;
		
		//2. ����ͳ�ơ�
		int flow = zjmsg.length();
		rtu.incDownGprsCount();	rtu.addDownGprsFlowmeter(flow);
		
		//3. ԭʼ���ı��棬ע�⣬������ݿⷱæ��ԭʼ���Ŀ��ܻᶪ����
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
	}

	public void setMsgQueue(FEMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}

	public void setUdefQueue(UserDefineMessageQueue udefQueue) {
		this.udefQueue = udefQueue;
	}

	public void setHeartBeat(Object heartBeat) {
//		this.heartBeat = (HeartBeatMessage)heartBeat;
	}
	public final void setAsyncDbService(AsyncService asyncDbService) {
		this.asyncDbService = asyncDbService;
	}
	
}
