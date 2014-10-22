/**
 * UDPͨ���������ա��������ࡣ
 * UDP���������õ��̶߳����ݡ�
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
		/** �Ӽ����Ͻ���buf���ݿ��ܰ������֡�����ݣ������Ҫѭ������
		 *  ��������ȡ���ݺ󣨿��ܲ������ݣ���������ʣ�����ݣ�����Ҫɾ���Ѿ�����������ݣ�
		 *  Ȼ������buf����д��
		 */
		ByteBuffer buf = client.getBufRead();
		while(buf.hasRemaining()){ //��ѭ����ر����������ģ������о��㷨��
			IMessage msg = client.getCurReadingMsg();
			if( null == msg ){
				//client��ǰû����Ϣ������Ҫ��������Ϣ
				msg = client.getServer().createMessage();
				if( null == msg ){
					//�����������ܴ�����Ϣ������Ϣ�����������ô���
					String info = "��Ϣ�����������ô���,UDP server port="+client.getServer().getPort();
					log.fatal(info);
					buf.clear();	//��ջ��������Ա��´�д��
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
				//�ڲ���ģʽ�£�����Ϣֱ�ӷ��͸�client���Ա���Թ���֪������ԭ��
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
						log.warn("����ģʽ��UDPӦ���쳣:"+e.getLocalizedMessage(),e);
					}
				}
				//��Ϣ��ȡ�쳣�������Ҫ���¶�ȡ�����ǵ�����׳�ԣ��´ζ�ȡ�µ���Ϣ��
				client.setCurReadingMsg(null);
				return false;
			}
			if( down ){		//��Ϣ�Ѿ�������ȡ��
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
		//ע�⣬���������ܻ���ʣ������û�б�����ʣ�������Ƶ�ǰ�棬���Լ���put���ݡ�
		//����������������ݶ��������꣬��ôcompact�൱��clear������Ҫ��.
		if( buf.hasRemaining() )
			buf.compact();
		else
			buf.clear();
		return true;
	}

}
