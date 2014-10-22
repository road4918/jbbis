/**
 * UDP通道简单数据收、发处理类。
 * UDP服务器采用单线程读数据。
 */
package com.hzjbbis.fk.sockserver.io;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.spi.socket.IClientIO;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.exception.MessageParseException;
import com.hzjbbis.fk.exception.SocketClientCloseException;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;

/**
 * @author bhw
 *	2008-06-05 16:11
 */
public class SimpleUdpIoHandler implements IClientIO {
	private static final Logger log = Logger.getLogger(SimpleUdpIoHandler.class);

	public boolean onSend(IServerSideChannel client)
			throws SocketClientCloseException {
		return false;
	}

	public boolean onReceive(IServerSideChannel client) throws SocketClientCloseException 
	{
		/** 从技术上讲，buf内容可能包含多个帧的数据，因此需要循环处理。
		 *  缓冲区读取数据后（可能部分数据，还有少量剩余数据），需要删除已经被处理的数据，
		 *  然后允许buf继续写。
		 */
		ByteBuffer buf = client.getBufRead();
		while(buf.hasRemaining()){ //该循环务必保留。不理解的，继续研究算法。
			IMessage msg = client.getCurReadingMsg();
			if( null == msg ){
				//client当前没有消息对象，需要创建新消息
				msg = client.getServer().createMessage();
				if( null == msg ){
					//服务器对象不能创建消息对象。消息对象类型配置错误。
					String info = "消息对象类型配置错误,UDP server port="+client.getServer().getPort();
					log.fatal(info);
					buf.clear();	//清空缓冲区，以便下次写。
					throw new SocketClientCloseException(info);
				}
				
				client.setCurReadingMsg(msg);
				msg.setSource(client);
				msg.setServerAddress(client.getServer().getServerAddress());
			}
			boolean down = false;
			try{
				down = msg.read(buf);
			}catch(MessageParseException mpe){
				String expInfo = mpe.getLocalizedMessage();
				//在测试模式下，把信息直接发送给client，以便测试工具知道错误原因。
				if( FasSystem.getFasSystem().isTestMode() ){
					SocketAddress sa = client.getSocketAddress();
					if( null == sa )
						return false;
					byte[] expBytes = expInfo.getBytes();
					try{
						DatagramSocket ds = new DatagramSocket();
						DatagramPacket dp = new DatagramPacket(expBytes,expBytes.length,sa);
						ds.send(dp);
					}catch(Exception e){
						log.warn("测试模式下UDP应答异常:"+e.getLocalizedMessage(),e);
					}
				}
				//消息读取异常，因此需要重新读取。考虑到程序健壮性，下次读取新的消息。
				client.setCurReadingMsg(null);
				return false;
			}
			if( down ){		//消息已经完整读取。
				client.setCurReadingMsg(null);
				msg.setIoTime(System.currentTimeMillis());
				msg.setPeerAddr(client.getPeerAddr());
				msg.setTxfs(client.getServer().getTxfs());
				ReceiveMessageEvent ev = new ReceiveMessageEvent(msg,client);
				GlobalEventHandler.postEvent( ev );
			}
			else
				break;
		}
		//注意，缓冲区可能还有剩余数据没有被处理。剩余数据移到前面，可以继续put数据。
		//如果缓冲区所有数据都被处理完，那么compact相当于clear。满足要求.
		if( buf.hasRemaining() )
			buf.compact();
		else
			buf.clear();
		return true;
	}

}
