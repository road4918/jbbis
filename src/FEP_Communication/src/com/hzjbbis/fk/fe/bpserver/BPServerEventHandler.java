/**
 * 业务处理器Socket服务器的事件处理器
 * 功能概述：
 * 		侦听收到业务处理器(BP)下行报文、上行报文发送成功事件。
 *      业务处理器下行报文通过FEMessageQueue对象方法直接发送给网关，网关再直接发送给终端；
 * 技术实现：
 * BasicEventHook派生类。
 * override handleEvent方法，针对ReceiveMessageEvent和SendMessageEvent特别处理。
 * 注意事项：在spring配置文件中，source对象必须是业务处理器服务接口的SocketServer对象。
 */
package com.hzjbbis.fk.fe.bpserver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.fe.msgqueue.FEMessageQueue;
import com.hzjbbis.fk.fe.msgqueue.MessageDispatch2Bp;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.sockserver.event.AcceptEvent;
import com.hzjbbis.fk.sockserver.event.ClientCloseEvent;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;
import com.hzjbbis.fk.tracelog.TraceLog;

/**
 * @author bhw
 * 2008-06-06 11:03
 */
public class BPServerEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(BPServerEventHandler.class);
	private static final TraceLog trace = TraceLog.getTracer(BPServerEventHandler.class);
	private FEMessageQueue msgQueue;
	private boolean noConvert = false;		//是否直接上行浙江规约原始报文.
	private boolean dispatchRandom = true;
	
	//内部属性
	private List<IServerSideChannel> bpClients = Collections.synchronizedList(new ArrayList<IServerSideChannel>());

	public BPServerEventHandler(){
	}
	
	@Override
	public boolean start() {
		return super.start();
	}
	
	public void setMsgQueue(FEMessageQueue queue) {
		this.msgQueue = queue;
		msgQueue.setDispatchRandom(dispatchRandom);
		msgQueue.setNoConvert(noConvert);
	}
	
	public FEMessageQueue getMsgQueue(){
		return msgQueue;
	}
	
	/**
	 * 重载该方法。
	 */
	public void handleEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			//当收到业务处理器下行报文
			onRecvMessage( (ReceiveMessageEvent)e);
		}
		else if( e.getType() == EventType.MSG_SENT ){
			//当成功把报文发送给业务处理器
			onSendMessage( (SendMessageEvent)e );
		}
		else if( e.getType() == EventType.ACCEPTCLIENT ){
			//对于网络异常断开情况，CLIENTCLOSE事件可能不会发生，将导致bpClients存放垃圾
			//每次删除1个即可达到高效清理垃圾。
			for(int i=0; i<bpClients.size(); i++ ){
				try{
					IServerSideChannel client = bpClients.get(i);
					if( System.currentTimeMillis()-client.getLastIoTime() > 1000*60*30 ){
						bpClients.remove(i);
						if( trace.isEnabled() )
							trace.trace("garbage client removed:"+client);
						break;
					}
				}catch(Exception exp){
					break;
				}
			}
			AcceptEvent ae = (AcceptEvent)e;
			bpClients.add(ae.getClient());
			//msgQueue.onBpClientConnected(ae.getClient());
		}
		else if( e.getType() == EventType.CLIENTCLOSE ){
			ClientCloseEvent ce = (ClientCloseEvent)e;
			bpClients.remove(ce.getClient());
			msgQueue.onBpClientClosed(ce.getClient());
		}
		else if( e.getType() == EventType.MSG_SEND_FAIL ){
			//当client被关闭，而sendList有报文，将被回收。
			msgQueue.pushBack(e.getMessage());
		}
		else
			super.handleEvent(e);
	}

	/**
	 * 收到业务处理器的下行报文
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		//对于网关规约报文，需要转换成浙江规约，才可以发送给浙江终端。
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				IServerSideChannel client = (IServerSideChannel)msg.getSource();
				//only business processor can send heart-beat to FE. Manufacture module send MessageZj.
				//If there is a client channel in bpClients, then fire msgQueue onConnect event
				if( bpClients.remove(client) )
					msgQueue.onBpClientConnected(client);
				
				//读取客户端请求的报文数量
				ByteBuffer data = mgate.getData();
				int numPackets = data.remaining()<4 ? -1 : data.getInt();
				synchronized(client){
					client.setRequestNum(numPackets);
				}
				//应答请求
				MessageGate hreply = MessageGate.createHReply();
				client.send(hreply);
				return;		//心跳处理结束
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				MessageZj zjmsg = mgate.getInnerMessage();
				//指定短信通道下行，需要在sendMessage方法判断MessageGate。
				zjmsg.setPeerAddr(mgate.getSource().getPeerAddr());	//当发送成功后zjmsg.peerAddr属性为下行报文来源地址
				boolean success = msgQueue.sendMessage(mgate);
				if( success && log.isDebugEnabled() )
					log.debug("业务处理器下行命令:"+zjmsg);
			}
			else if( mgate.getHead().getCommand() == MessageGate.REP_MONITOR_RELAY_PROFILE ){
				String bpProfile = new String(mgate.getData().array());
				FasSystem.getFasSystem().addBizProcessorProfile(e.getClient().getPeerAddr(), bpProfile);
				return;
			}
			else if( mgate.getHead().getCommand() == MessageGate.CMD_WRAP_ZJ ){
				//浙江规约帧直接下行，估计是厂家解析模块。直接下行命令
				MessageZj zjmsg = mgate.getInnerMessage();
				if( null == zjmsg )
					return;
				//当发送成功后zjmsg.peerAddr属性为下行报文来源地址
				zjmsg.setPeerAddr(mgate.getSource().getPeerAddr());
				boolean success = msgQueue.sendMessage(zjmsg);
				if( success && log.isDebugEnabled() )
					log.info("厂家解析模块下行命令:"+zjmsg);
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			MessageZj zjmsg = (MessageZj)msg;
			boolean success = msgQueue.sendMessage(zjmsg);
			if( success && log.isDebugEnabled() )
				log.debug("业务处理器下行命令:"+zjmsg);
		}
	}
	
	/**
	 * 往业务处理器上行报文成功。
	 * @param e
	 */
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		//特别注意：如果是厂家自定义报文发送成功，不需要后续发送。
		//因为收到厂家自定义报文时，会自动记住下行通道与厂家编码关系，应答报文依赖这个关系自动发送给解析模块。
		if( msg instanceof MessageZj ){
			MessageZj zjmsg = (MessageZj)msg;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
				if( log.isDebugEnabled() )
					log.debug("往厂家解析模块发送报文成功:"+zjmsg.getRawPacketString());
				return;
			}
		}
		if( log.isDebugEnabled() )
			log.debug("往业务处理器发送报文成功:"+msg);
		//报文发送到业务处理器后，需要检测后续发送。
		//2009－1－18 modified by bhw。支持多业务处理器，按照地市分发。
		if( dispatchRandom ){
			IServerSideChannel client = (IServerSideChannel)e.getClient();
			trySendNextPacket(client);
		}
		else{
			//按照地市均衡分发到业务处理器
			trySendNextPacketByA1();
		}
	}
	
	/**
	 * 按照队列消息的地市码进行分发到业务处理器。
	 */
	private void trySendNextPacketByA1(){
		MessageZj msg = (MessageZj)msgQueue.poll();
		if( null == msg )
			return;
		IServerSideChannel client = MessageDispatch2Bp.getInstance().getBpChannel(msg.head.rtua_a1);
		if( null == client ){
			msgQueue.pushBack(msg);
			return;
		}
		if( 0 >= client.getRequestNum() )
			return;
		boolean success = false;
		if( noConvert ){
			success = client.send(msg);
		}
		else{
			//把浙江规约报文转换成网关规约，发送给前置机。
			MessageGate gateMsg = new MessageGate();
			gateMsg.setUpInnerMessage(msg);
			success = client.send(gateMsg);
		}
		if( !success ){
			msgQueue.pushBack(msg);
		}
	}
	
	private void trySendNextPacket(IServerSideChannel client){
		//检测client的请求数量是否递减至0？
		if( 0 >= client.getRequestNum() ){
			//不能发送。
			return;
		}
		IMessage msg = msgQueue.poll();
		if( null != msg ){
			boolean success = false;
			if( noConvert )
				success = client.send(msg);
			else{
				//把浙江规约报文转换成网关规约，发送给前置机。
				MessageGate gateMsg = new MessageGate();
				gateMsg.setUpInnerMessage(msg);
				success = client.send(gateMsg);
			}
			if( !success ){
				msgQueue.pushBack(msg);
			}
		}
	}
	
	public boolean isNoConvert() {
		return noConvert;
	}

	public void setNoConvert(boolean noConvert) {
		this.noConvert = noConvert;
		if( null != msgQueue )
			msgQueue.setNoConvert(noConvert);
	}
	
	public void setDispatchRandom(boolean dispRandom ){
		dispatchRandom = dispRandom;
		if( null != msgQueue )
			msgQueue.setDispatchRandom(dispatchRandom);
	}
}
