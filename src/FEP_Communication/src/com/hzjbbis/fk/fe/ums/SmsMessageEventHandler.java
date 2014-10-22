/**
 * 用于通信前置机与短信网关之间报文收发事件处理。
 * 上行报文进入优先级队列，以便业务处理器处理。
 * 注意处理流量统计、工况等。
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
 * 2008－10－20
 */
public class SmsMessageEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(SmsMessageEventHandler.class);
	private IMessageQueue msgQueue;				//spring 配置实现。与GateMessageEventHandler同一个对象。
	private UserDefineMessageQueue udefQueue;	//spring 配置实现。厂家解析模块上行消息队列
	private AsyncService asyncDbService;		//用于批量保存原始报文
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SEND_FAIL) )
			onSendFailMessage( (MessageSendFailEvent)event );
	}

	/**
	 * 收到UMS网关的上行报文。
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent event){
		MessageZj zjmsg = (MessageZj)event.getMessage();
		if( log.isDebugEnabled() )
			log.debug("UMS短信网关上行报文:"+zjmsg);
		/** 通信前置机收到UMS网关上行报文，需要特殊处理
		 *  （1）心跳报文不支持的。
		 *  （2）终端工况：流量
		 *  （3）更新终端所属的网关appid：subid，与主站配置不一致处理
		 */
		//1. 从终端对象管理器找到终端对象
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
		if( null == rtu ){
			String strRtua = HexDump.toHex(zjmsg.head.rtua);
			log.warn("短信通道上行，找不到对应终端。appid="+zjmsg.getPeerAddr()+",msg="+zjmsg.getRawPacketString());
			rtu = new ComRtu();
			rtu.setLogicAddress(strRtua);
			rtu.setRtua(zjmsg.head.rtua);
			RtuManage.getInstance().putComRtuToCache(rtu);
		}
		
		//2. 更新工况
		rtu.setLastSmsTime(System.currentTimeMillis());
		rtu.setLastIoTime(rtu.getLastSmsTime());
		
		//3. 流量统计。
		rtu.incUpSmsCount();
		
		//4.1 更新终端上行手机号码(simNum) activeUms activeSubAppId
		//短信上行，已经设置如下：msg.setServerAddress(from+","+receiver);
		boolean channelChanged = false;			//短信当前通道发生变化，需要检测不一致性
		String serverAddr = zjmsg.getServerAddress();
		String appid = event.getClient().getPeerAddr();	//网关的前置机接口地址。
		int index = serverAddr.indexOf(',');
		try{
			String upMobile = serverAddr.substring(0, index);	//消息上行的手机号码
			String receiver = serverAddr.substring(index+1);	//可能含子应用号, 95598340102
			//如果终端的手机号码发生变化，需要修改终端参数，同时通知RtuParamsCache进行缓存。
			boolean updateRtuCache = false;
			if( !upMobile.equals(rtu.getSimNum()) ){
				rtu.setUpMobile(upMobile);	rtu.setSimNum(upMobile);
				updateRtuCache = true;
			}
			
			//下面更新activeUms activeSubAppId
			index = receiver.indexOf("95598");
			if( index==0 )
				receiver = receiver.substring(5);
			if( ! appid.equals(rtu.getActiveUms()) ){
				rtu.setActiveUms(appid);
				channelChanged = true;
				updateRtuCache = true;
			}
			//确保appid不含95598。
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
		
		//4.2 检测通信参数不一致
		if( channelChanged ){
			//只有通道发生变化的终端，才需要检测参数是否不一致。
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
					rtu.setMisSmsAddress(appid);		//终端实际通道地址与资产表不一致。
			}catch(Exception e){
				log.error("search discord SMS params exp:"+e.getLocalizedMessage(),e);
			}
			
		}

		//5.1 更新业务信息
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK ){
			rtu.incTaskCount();
		}
		else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
			//产生心跳保存业务事件
			//短信通道无心跳应答。
		}
		
		//5.2 记录原始报文: 短信上行原始报文
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
		
		//6. 厂家自定义报文，需要直接发送给厂家。不能按照目前的主动取这个模式。
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
			udefQueue.offer(zjmsg);
			return;
		}
		//7. 报文进行上行队列，以便发送给业务处理器。
		msgQueue.offer(zjmsg);
	}
	
	private void onSendMessage(SendMessageEvent event){
		MessageZj zjmsg = (MessageZj)event.getMessage();
		if( log.isDebugEnabled() )
			log.debug("UMS短信网关下行报文:"+zjmsg);
		/** 通信前置机下行报文到UMS网关，需要特殊处理
		 *  （1）心跳报文不支持的。
		 *  （2）终端工况：流量
		 *  （3）更新终端所属的网关appid：subid，与主站配置不一致处理
		 */
		//1. 从终端对象管理器找到终端对象
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
		//1.1 如果是普通用户短信发送，则没有对应终端
		if( null == rtu )
			return;
		
		//2. 更新工况
		rtu.setLastIoTime(System.currentTimeMillis());
		
		//3. 流量统计。
		rtu.incDownSmsCount();

		//4. 终端归属网关 关系更新
		try{
			String appid = event.getClient().getPeerAddr();	//网关的前置机接口地址。
			//还需要从msg提取subappid
			if( ! appid.equals(rtu.getActiveUms()) ){
				rtu.setActiveUms(appid);
			}
		}catch(Exception err){
			log.error(err.getLocalizedMessage(),err);
		}
		
		//5. 原始报文保存
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
		
		//6. 厂家自定义报文，需要直接发送给厂家。不能按照目前的主动取这个模式。
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
			udefQueue.offer(zjmsg);
			return;
		}
	}
	
	private void onSendFailMessage( MessageSendFailEvent event ){
		MessageZj zjmsg = (MessageZj)event.getMessage();
		zjmsg.setStatus("1");		//'1'表示发送失败，null or '0'表示成功。
		if( log.isDebugEnabled() )
			log.debug("UMS短信网关下行报文:"+zjmsg);
		
		//1. 原始报文保存
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
