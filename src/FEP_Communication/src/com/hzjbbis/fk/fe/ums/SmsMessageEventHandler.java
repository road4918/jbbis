/**
 * ����ͨ��ǰ�û����������֮�䱨���շ��¼�����
 * ���б��Ľ������ȼ����У��Ա�ҵ����������
 * ע�⴦������ͳ�ơ������ȡ�
 */
package com.hzjbbis.fk.fe.ums;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.common.spi.IMessageQueue;
import com.hzjbbis.fk.fe.filecache.RtuParamsCache;
import com.hzjbbis.fk.fe.userdefine.UserDefineMessageQueue;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.sockserver.event.MessageSendFailEvent;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 * 2008��10��20
 */
public class SmsMessageEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(SmsMessageEventHandler.class);
	private IMessageQueue msgQueue;				//spring ����ʵ�֡���GateMessageEventHandlerͬһ������
	private UserDefineMessageQueue udefQueue;	//spring ����ʵ�֡����ҽ���ģ��������Ϣ����
	private AsyncService asyncDbService;		//������������ԭʼ����
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SEND_FAIL) )
			onSendFailMessage( (MessageSendFailEvent)event );
	}

	/**
	 * �յ�UMS���ص����б��ġ�
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent event){
		MessageZj zjmsg = (MessageZj)event.getMessage();
		if( log.isDebugEnabled() )
			log.debug("UMS�����������б���:"+zjmsg);
		/** ͨ��ǰ�û��յ�UMS�������б��ģ���Ҫ���⴦��
		 *  ��1���������Ĳ�֧�ֵġ�
		 *  ��2���ն˹���������
		 *  ��3�������ն�����������appid��subid������վ���ò�һ�´���
		 */
		//1. ���ն˶���������ҵ��ն˶���
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
		if( null == rtu ){
			String strRtua = HexDump.toHex(zjmsg.head.rtua);
			log.warn("����ͨ�����У��Ҳ�����Ӧ�նˡ�appid="+zjmsg.getPeerAddr()+",msg="+zjmsg.getRawPacketString());
			rtu = new ComRtu();
			rtu.setLogicAddress(strRtua);
			rtu.setRtua(zjmsg.head.rtua);
			RtuManage.getInstance().putComRtuToCache(rtu);
		}
		
		//2. ���¹���
		rtu.setLastSmsTime(System.currentTimeMillis());
		rtu.setLastIoTime(rtu.getLastSmsTime());
		
		//3. ����ͳ�ơ�
		rtu.incUpSmsCount();
		
		//4.1 �����ն������ֻ�����(simNum) activeUms activeSubAppId
		//�������У��Ѿ��������£�msg.setServerAddress(from+","+receiver);
		boolean channelChanged = false;			//���ŵ�ǰͨ�������仯����Ҫ��ⲻһ����
		String serverAddr = zjmsg.getServerAddress();
		String appid = event.getClient().getPeerAddr();	//���ص�ǰ�û��ӿڵ�ַ��
		int index = serverAddr.indexOf(',');
		try{
			String upMobile = serverAddr.substring(0, index);	//��Ϣ���е��ֻ�����
			String receiver = serverAddr.substring(index+1);	//���ܺ���Ӧ�ú�, 95598340102
			//����ն˵��ֻ����뷢���仯����Ҫ�޸��ն˲�����ͬʱ֪ͨRtuParamsCache���л��档
			boolean updateRtuCache = false;
			if( !upMobile.equals(rtu.getSimNum()) ){
				rtu.setUpMobile(upMobile);	rtu.setSimNum(upMobile);
				updateRtuCache = true;
			}
			
			//�������activeUms activeSubAppId
			index = receiver.indexOf("95598");
			if( index==0 )
				receiver = receiver.substring(5);
			if( ! appid.equals(rtu.getActiveUms()) ){
				rtu.setActiveUms(appid);
				channelChanged = true;
				updateRtuCache = true;
			}
			//ȷ��appid����95598��
			if( appid.startsWith("95598"))
				appid = appid.substring(5);
			String subAppId = null;
			if( receiver.length()> appid.length() ){
				subAppId = receiver.substring(appid.length());
				if( ! subAppId.equals(rtu.getActiveSubAppId()) ){
					rtu.setActiveSubAppId(subAppId);
					updateRtuCache = true;
				}
			}
			else{
				if( null != rtu.getActiveSubAppId() && rtu.getActiveSubAppId().length()>0 ){
					rtu.setActiveSubAppId(null);
					updateRtuCache = true;
				}
			}
			
			if( updateRtuCache )
				RtuParamsCache.getInstance().addRtu(rtu);
		}catch(Exception e){
			log.error("update RTU:(simNum activeUms activeSubAppId) exception:"+e.getLocalizedMessage(),e);
		}
		
		//4.2 ���ͨ�Ų�����һ��
		if( channelChanged ){
			//ֻ��ͨ�������仯���նˣ�����Ҫ�������Ƿ�һ�¡�
			try{
				ArrayList<String> smsAddrs = new ArrayList<String>();
				String addr = null;
				if( appid.startsWith("95598"))
					appid = appid.substring(5);
				if( "01".equals(rtu.getCommType())){
					addr = rtu.getCommAddress();
					if( null != addr ){
						if( addr.startsWith("95598"))
							addr = addr.substring(5);
						if( addr.length()>appid.length() )
							addr = addr.substring(0, appid.length());
						smsAddrs.add(addr);
					}
				}
				if( "01".equals(rtu.getB1CommType())){
					addr = rtu.getB1CommAddress();
					if( null != addr ){
						if( addr.startsWith("95598"))
							addr = addr.substring(5);
						if( addr.length()>appid.length() )
							addr = addr.substring(0, appid.length());
						smsAddrs.add(addr);
					}
				}
				if( "01".equals(rtu.getB2CommType())){
					addr = rtu.getB2CommAddress();
					if( null != addr ){
						if( addr.startsWith("95598"))
							addr = addr.substring(5);
						if( addr.length()>appid.length() )
							addr = addr.substring(0, appid.length());
						smsAddrs.add(addr);
					}
				}
				boolean same = smsAddrs.size()==0 ;
				for(String smsAddr: smsAddrs){
					if( smsAddr.startsWith(appid)){
						same = true;
						break;
					}
				}
				
				if( ! same )
					rtu.setMisSmsAddress(appid);		//�ն�ʵ��ͨ����ַ���ʲ���һ�¡�
			}catch(Exception e){
				log.error("search discord SMS params exp:"+e.getLocalizedMessage(),e);
			}
			
		}

		//5.1 ����ҵ����Ϣ
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK ){
			rtu.incTaskCount();
		}
		else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
			//������������ҵ���¼�
			//����ͨ��������Ӧ��
		}
		
		//5.2 ��¼ԭʼ����: ��������ԭʼ����
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
		
		//6. �����Զ��屨�ģ���Ҫֱ�ӷ��͸����ҡ����ܰ���Ŀǰ������ȡ���ģʽ��
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
			udefQueue.offer(zjmsg);
			return;
		}
		//7. ���Ľ������ж��У��Ա㷢�͸�ҵ��������
		msgQueue.offer(zjmsg);
	}
	
	private void onSendMessage(SendMessageEvent event){
		MessageZj zjmsg = (MessageZj)event.getMessage();
		if( log.isDebugEnabled() )
			log.debug("UMS�����������б���:"+zjmsg);
		/** ͨ��ǰ�û����б��ĵ�UMS���أ���Ҫ���⴦��
		 *  ��1���������Ĳ�֧�ֵġ�
		 *  ��2���ն˹���������
		 *  ��3�������ն�����������appid��subid������վ���ò�һ�´���
		 */
		//1. ���ն˶���������ҵ��ն˶���
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
		//1.1 �������ͨ�û����ŷ��ͣ���û�ж�Ӧ�ն�
		if( null == rtu )
			return;
		
		//2. ���¹���
		rtu.setLastIoTime(System.currentTimeMillis());
		
		//3. ����ͳ�ơ�
		rtu.incDownSmsCount();

		//4. �ն˹������� ��ϵ����
		try{
			String appid = event.getClient().getPeerAddr();	//���ص�ǰ�û��ӿڵ�ַ��
			//����Ҫ��msg��ȡsubappid
			if( ! appid.equals(rtu.getActiveUms()) ){
				rtu.setActiveUms(appid);
			}
		}catch(Exception err){
			log.error(err.getLocalizedMessage(),err);
		}
		
		//5. ԭʼ���ı���
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
		
		//6. �����Զ��屨�ģ���Ҫֱ�ӷ��͸����ҡ����ܰ���Ŀǰ������ȡ���ģʽ��
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
			udefQueue.offer(zjmsg);
			return;
		}
	}
	
	private void onSendFailMessage( MessageSendFailEvent event ){
		MessageZj zjmsg = (MessageZj)event.getMessage();
		zjmsg.setStatus("1");		//'1'��ʾ����ʧ�ܣ�null or '0'��ʾ�ɹ���
		if( log.isDebugEnabled() )
			log.debug("UMS�����������б���:"+zjmsg);
		
		//1. ԭʼ���ı���
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
	}

	public void setMsgQueue(IMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}

	public void setUdefQueue(UserDefineMessageQueue udefQueue) {
		this.udefQueue = udefQueue;
	}

	public final void setAsyncDbService(AsyncService asyncDbService) {
		this.asyncDbService = asyncDbService;
	}
}
