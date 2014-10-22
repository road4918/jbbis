/**
 * ����첽TCP��������UDP�������˵�socket clientͨ������ӿڶ��塣
 */
package com.hzjbbis.fk.common.spi.socket;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 * @version 1.0
 */
public interface IServerSideChannel extends IChannel{
	/**
	 * ����tcp����client������socket channel
	 * @return
	 */
	SocketChannel getChannel();
	
	/**
	 * ClientChannel����д������¼���ÿ�ζ�����󣬳���д������0��
	 * ���ڶ���д��Ϣ�����ȼ����ơ�
	 * @return
	 */
	int getLastingWrite();
	void setLastingWrite(int writeCount);
	
	/**
	 * ����UDP���� client,���� SocketAddress
	 * @return
	 */
	SocketAddress getSocketAddress();
	
	/**
	 * ���ڹ���socket clientͨ���ĵ�ǰMessage����д��
	 * ��IClientIO�ӿڵ��á�
	 * @return
	 */
	IMessage getCurReadingMsg();
	void setCurReadingMsg(IMessage curReadingMsg);
	IMessage getCurWritingMsg();
	void setCurWritingMsg(IMessage curWritingMsg);
	//Modified by bhw 2009-1-17 15:29 to avoid message resend. 2 function added.
	//Affected class is AsyncSocketClient and UdpClient.
	/**
	 * �������Ƿ���������Ҫ��������
	 * @return
	 */
	boolean bufferHasRemaining();
	void setBufferHasRemaining(boolean hasRemaining);
	/**
	 * ȡ����Ҫ��client�������͵�����Ϣ����
	 * @return
	 */
	IMessage getNewSendMessage();
	
	/**
	 * �첽tcp server��UDP server��client channnel��д��������
	 * @return
	 */
	ByteBuffer getBufRead();
	ByteBuffer getBufWrite();
}
