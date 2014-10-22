/**
 * 厂家自定义报文的上行队列以及下行通道。
 * 
 */
package com.hzjbbis.fk.fe.userdefine;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.fe.ChannelManage;
import com.hzjbbis.fk.message.zj.MessageZj;

/**
 * @author bhw
 *
 */
public class UserDefineMessageQueue {
	private static final Logger log = Logger.getLogger(UserDefineMessageQueue.class);
	private static final UserDefineMessageQueue instance = new UserDefineMessageQueue();
	private Map<Byte,IChannel> userMap = new HashMap<Byte,IChannel>();
	private UserDefineMessageQueue(){}
	
	public static final UserDefineMessageQueue getInstance(){
		return instance;
	}
	
	public void offer(MessageZj msg){
		//当GPRS网关收到用户自定义报文上行，则把报文直接发送给厂家解析模块
		//按照厂家编码，把报文推送给厂家升级模块。
		IChannel srcChannel = userMap.get(msg.head.msta);
		if( null == srcChannel ){
			log.error("收到厂家自定义报文，但厂家解析模块与通信前置机的连接找不到。msg="+msg.getRawPacketString());
			return;
		}
		srcChannel.send(msg);
		//厂家自定义报文记录日志
		log.info("厂家自定义报文应答："+msg.getRawPacketString());
	}
	
	/**
	 * 厂家解析模块，连接到通信前置机的某个服务端口。
	 * 因此这里的MessageZj的source，一定是异步socket client对象。
	 * @param msg
	 * @return
	 */
	public boolean sendMessageDown(MessageZj msg){
		//1. 按照下行的报文，管理厂家编码与厂家解析模块到通信前置机之间的clientChannel。
		IChannel srcChannel = (IChannel)msg.getSource();
		userMap.put(msg.head.msta, srcChannel);
		//2. 选择网关通道，直接下行。
		IChannel channel = ChannelManage.getInstance().getChannel(msg.head.rtua);
		if( null == channel )
			return false;
		//短信终端不支持自动升级
		channel.send(msg);
		return true;
	}
}
