/**
 * 用于网关与通信前置机之间报文收发事件处理。
 * 上行报文进入优先级队列，以便业务处理器处理。
 * 注意处理流量统计、工况等。
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
	private FEMessageQueue msgQueue;	//spring 配置实现。
	private UserDefineMessageQueue udefQueue;	//spring 配置实现。厂家解析模块上行消息队列
	private AsyncService asyncDbService;		//用于批量保存原始报文
	
//	private HeartBeatMessage heartBeat;//spring 配置实现，心跳保存
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}
	/**
	 * 收到GPRS网关的上行报文。
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){
				//客户端请求的报文数量的应答
				log.info(mgate);
				return;		//心跳处理结束
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY ){
				MessageZj zjmsg = mgate.getInnerMessage();
				_handleZjMessage(zjmsg,e);
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_SENDFAIL ){
				//GPRS网关下行失败，需要把请求通过短信通道下行到终端。
				MessageZj zjmsg = mgate.getInnerMessage();
				//如不走短信:厂家自定义报文
				if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE 
						|| zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART )
					return;

				//GPRS下行失败，需要转短信。制定短信下行策略.
				if( null != zjmsg && log.isDebugEnabled() )
					log.debug("网关下行失败报文,转短信通道:"+zjmsg);
				msgQueue.sendMessageByUms(zjmsg);
				return;
			}
			else if( mgate.getHead().getCommand() == MessageGate.REP_MONITOR_RELAY_PROFILE ){
				String gateProfile = new String(mgate.getData().array());
				FasSystem.getFasSystem().addGprsGateProfile(e.getClient().getPeerAddr(), gateProfile);
				return;
			}
			else {
				//其它类型命令
				log.error("其它类型命令。");
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			_handleZjMessage((MessageZj)msg,e);
		}
	}
	
	private void _handleZjMessage(MessageZj zjmsg,ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("网关上行报文:"+zjmsg);
		/** 通信前置机收到网关上行报文，需要特殊处理
		 *  （1）心跳报文存MySQL；
		 *  （2）终端工况：流量
		 *  （3）更新终端所属的网关IP：port，与主站配置不一致处理
		 */
		//1. 从终端对象管理器找到终端对象
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
		if( null == rtu ){
//			UndocRtuMsgCache.addMessage(zjmsg);
			String strRtua = HexDump.toHex(zjmsg.head.rtua);
			log.warn("终端不在缓存中，需要重新加载。rtua=" + strRtua );
			//新增终端情况下，需要增加一个对象
			rtu = new ComRtu();
			rtu.setLogicAddress(strRtua);
			rtu.setRtua(zjmsg.head.rtua);
//			rtu.setDeptCode("0");
			RtuManage.getInstance().putComRtuToCache(rtu);
		}
		
		//需要从数据库加载
		
		//2. 更新工况
		rtu.setLastGprsTime(System.currentTimeMillis());
		rtu.setLastIoTime(rtu.getLastGprsTime());
		//终端GPRS地址
		String gprsIpAddr = zjmsg.getPeerAddr();
		if( null != gprsIpAddr && gprsIpAddr.length()>0 )
			rtu.setRtuIpAddr(gprsIpAddr);
		
		//3. 流量统计。心跳自动回复，所以长度*2,登录回复
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
		
		//4. 终端归属网关 关系更新
		try{
			String gateAddr = event.getClient().getPeerAddr();	//网关的前置机接口地址。
			if( ! gateAddr.equals(rtu.getActiveGprs()) ){
				rtu.setActiveGprs(gateAddr);
				//终端所连接的网关服务器发生变化，需要快速更新缓存文件，或者定期更新数据库
				//初始化时，需要从数据库加载。
				RtuParamsCache.getInstance().addRtu(rtu);		//终端参数缓存
				
				//需要检查GPRS地址不一致的情况。
				String serverAddr = zjmsg.getServerAddress();
				if( null != serverAddr && null != rtu.getCommAddress() ){
					if( "02".equals(rtu.getCommType())){
						if( ! serverAddr.equals(rtu.getCommAddress()) ){
							rtu.setMisGprsAddress(serverAddr);
							log.warn("终端实际上行地址与资产表不一致：rtua="+HexDump.toHex(zjmsg.head.rtua)+",serverAddress="+serverAddr);
						}
					}
				}
			}
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}
		
		//5.1 原始报文保存
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
		//5.2 心跳报文保存
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
			//产生心跳保存业务事件
			HeartbeatPersist.getInstance().handleHeartbeat(rtu.getRtua());
//			heartBeat.putBeat(rtu.getLogicAddress(),zjmsg.getIoTime(),rtu.getDeptCode());
			return;
		}
		
		//6. 厂家自定义报文，需要直接发送给厂家。不能按照目前的主动取这个模式。
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
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				//客户端请求的报文数量的应答
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
		//统计流量
		//1. 从终端对象管理器找到终端对象
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(zjmsg.head.rtua);
		if( null == rtu )
			return;
		
		//2. 流量统计。
		int flow = zjmsg.length();
		rtu.incDownGprsCount();	rtu.addDownGprsFlowmeter(flow);
		
		//3. 原始报文保存，注意，如果数据库繁忙，原始报文可能会丢弃。
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
