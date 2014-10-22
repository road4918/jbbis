/**
 * 功能概述：
 * 		侦听收到终端上行报文、下行报文发送成功事件。
 *    终端上行报文进入MessageQueue队列，以便发送给前置机；
 *    终端下行报文成功事件，简单打印日志。无进一步处理需求。
 * 技术实现：
 * SimpleEventHandler派生类。
 * override handleEvent方法，针对ReceiveMessageEvent和SendMessageEvent特别处理。
 * 注意事项：在spring配置文件中，source对象必须是网关终端接口的SocketServer对象。
 */
package com.hzjbbis.fk.gate.event;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.gate.MessageQueue;
import com.hzjbbis.fk.gate.PrefixRtuManage;
import com.hzjbbis.fk.gate.RTUChannelManager;
import com.hzjbbis.fk.gate.event.autoreply.AutoReply;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;

/**
 * @author bhw
 *	2008－06－03 22：37
 */
public class GateRTUEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(GateRTUEventHandler.class);
	private int seq = 0;
	private MessageQueue queue;

	public void setQueue(MessageQueue queue) {
		this.queue = queue;
	}

	/**
	 * 重载该方法。
	 */
	public void handleEvent(IEvent e) {
		/** 网关终端服务收到报文，必须尽快发送给前置机。由于异步原因，不能直接调用
		 *  网关前置机服务Accept的client发送。需要放到前置机上行队列。
		 *  	1）当前置机连接到网关，通知上行队列发送上行报文；
		 *  	2）当前置机对应client成功发送上行报文，通知上行队列继续发送；
		 */
		if( e.getType() == EventType.MSG_RECV ){
			//浙江规约报文入队列
			long n1 = System.currentTimeMillis();
			ReceiveMessageEvent evt = (ReceiveMessageEvent)e;
			IMessage msg = e.getMessage();
			if( msg.getMessageType() == MessageType.MSG_ZJ ){
				MessageZj zjmsg = (MessageZj)msg;
				RTUChannelManager.addClient(zjmsg.head.rtua, evt.getClient());
			}
			long n2 = System.currentTimeMillis();
			if( n2-n1> 15 )
				log.warn("RTUChannelManager.addClient处理>N毫秒,time="+(n2-n1));
			queue.offerUpMessageInQueue(e.getMessage());
			long n3 = System.currentTimeMillis();
			if( n3-n2> 80 )
				log.warn("offer.UpMessageInQueue处理>N毫秒,time="+(n3-n2));
			
			//测试：生成自动应答，原消息返回。
			IMessage rep = AutoReply.reply(e.getMessage());
			long n4 = System.currentTimeMillis();
			if( n4-n3>15 )
				log.warn("AutoReply.reply处理>N毫秒, time="+(n4-n3));
			if( null != rep ){
				try{
					MessageZj zjmsg = (MessageZj)rep;
					if( null != zjmsg ){
						//本地配置文件的高科终端下行需要增加前导字符
						zjmsg.setPrefix(PrefixRtuManage.getInstance().getRtuPrefix(zjmsg.head.rtua));
						queue.sendDownMessage(zjmsg);
					}
				}catch(Exception exp){
					log.warn(exp.getLocalizedMessage(),exp);
				}
				//queue.sendDownMessage(rep);
				seq++;
				if( log.isDebugEnabled())
					log.debug("send msg="+seq+" msg="+rep);
			}
			long n5 = System.currentTimeMillis();
			if( n5-n3> 80 )
				log.warn("queue.sendDownMessage处理>N毫秒，＝"+(n5-n3));
		}
		super.handleEvent(e);
	}

}
