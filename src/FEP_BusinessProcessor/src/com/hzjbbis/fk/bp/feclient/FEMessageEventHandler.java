/**
 * 用于业务处理器与通信前置机之间报文收发事件处理。
 * 上行报文进入优先级队列。
 */
package com.hzjbbis.fk.bp.feclient;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.bp.msgqueue.BPMessageQueue;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;


/**
 * @author bhw
 *
 */
public class FEMessageEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(FEMessageEventHandler.class);	
	private BPMessageQueue msgQueue;	//spring 配置实现。
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}
	
	/**
	 * 收到通信前置机的上行报文。
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		MessageZj zjmsg = null;
		if (log.isDebugEnabled())
			log.debug("收到通信前置机的上行报文:"+msg.getRawPacketString());
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){
				//客户端请求的报文数量的应答
				return;		//心跳处理结束
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY ){
				zjmsg = mgate.getInnerMessage();
				_handleZjMessage(zjmsg,e);
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_SENDFAIL ){
				//GPRS网关下行失败，需要把请求通过短信通道下行到终端。
				zjmsg = mgate.getInnerMessage();
				//把内部协议的发送失败报文转换为浙江规约发送失败报文。
				zjmsg = zjmsg.createSendFailReply();
				_handleZjMessage(zjmsg,e);
			}
			else if( mgate.getHead().getCommand() == MessageGate.REQ_MONITOR_RELAY_PROFILE ){
				//前置机请求网关的profile
				String profile = FasSystem.getFasSystem().getProfile();
				MessageGate repMoniteProfile = MessageGate.createMoniteProfileReply(profile);
				msgQueue.sendMessage(repMoniteProfile);
				return;
			}
			else {
				//其它类型命令
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			_handleZjMessage((MessageZj)msg,e);
		}
	}
	
	private void _handleZjMessage(MessageZj zjmsg,ReceiveMessageEvent event){	
		//报文进行上行队列，以便发送给业务处理器。
		msgQueue.offer(zjmsg);
	}
	
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		MessageZj zjmsg = null;
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				//客户端请求的报文数量的应答
				return;
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				zjmsg = mgate.getInnerMessage();
			}
			else
				return;
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			zjmsg = (MessageZj)msg;
		}
		if( null == zjmsg )
			return;
		if (log.isDebugEnabled())
			log.debug("往通讯前置机发送下行:"+zjmsg.getRawPacketString());				
	}

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
}
