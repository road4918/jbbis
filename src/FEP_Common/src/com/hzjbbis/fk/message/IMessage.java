/**
 * ��Ϣ����Ľӿڡ�
 * ��������Ϣ����һ����������֣���Ϣͷ
 */
package com.hzjbbis.fk.message;

import java.nio.ByteBuffer;

import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.exception.MessageParseException;

/**
 * @author bao
 *
 */
public interface IMessage {
    // ��Ϣ���䷽����
    static final Integer DIRECTION_UP = new Integer(0);
    static final Integer DIRECTION_DOWN = new Integer(1);
    // ��Ϣ���ȼ�����
	static final int PRIORITY_LOW = 0;
	static final int PRIORITY_NORMAL = 1;
	static final int PRIORITY_HIGH = 2;
    static final int PRIORITY_VIP = 3;
    static final int PRIORITY_MAX = 5;
	
	static final int STATE_INVALID = -1;

	static final int STATE_READ_HEAD = 0x01;
	static final int STATE_READ_DATA = 0x02;
	static final int STATE_READ_TAIL = 0x03;
	static final int STATE_READ_DONE = 0x0F;
	
	static final int STATE_SEND_HEAD = 0x11;
	static final int STATE_SEND_DATA = 0x12;
	static final int STATE_SEND_TAIL = 0x13;
	static final int STATE_SEND_DONE = 0x2F;
	
	/**
	 * ��Ϣ������Դ��һ��ΪAsyncSocketClient����
	 * @return
	 */
	IChannel getSource();
	void setSource(IChannel src);
	
	/**
	 * ������Ϣ������
	 * @return
	 */
	MessageType getMessageType();
	
	/**
	 * ��Ϣ��ʶ������
	 * �ض���Ϣ����ӻ�����readBuffer��ȡ���ݣ����ж��Ƿ��������ġ�
	 * @param readBuffer ������ģ���ṩ�Ķ�������
	 * @return
	 *   true : �ɹ���ȡ�������ģ�
	 *   false���������е����ݲ����������������
	 * @throws MessageParseException
	 * 	 �����Ϣ��ȡ�����У��������ݸ�ʽ����ȷ����throw �쳣
	 */
	boolean read(ByteBuffer readBuffer)throws MessageParseException;
	
	/**
	 * ��Ϣ�����Ҫ���͵����ݷŵ����ͻ�����writeBuffer
	 * @param writeBuffer
	 * @return
	 * @throws MessageParseException
	 */
	boolean write(ByteBuffer writeBuffer);
	
	/**
	 * ��Ϣ��ͨѶʱ�䡣�������������յ����ĵ�ʱ�䡣
	 * @return
	 */
	long getIoTime();
	void setIoTime(long time);
	
	/**
	 * �Է�ͨѶ��ַ ip:port ��ʽ
	 * @return
	 */
	String getPeerAddr();
	void setPeerAddr(String peer);
	
	/**
	 * ����������˵����Ҫ֪���ն�ʵ�����ӵ�����IP��PORT��ַ��
	 * @return
	 */
	String getServerAddress();
	void setServerAddress(String serverAddress);
	
	/**
	 * ȡ����Ϣ��ͨ�ŷ�ʽ
	 * @return
	 */
	String getTxfs();
	void setTxfs(String fs);
	
	/**
	 * ȡ����Ϣ����֡���
	 * @return
	 */
	public String getStatus();
	/**
	 * ȡ����Ϣ����������ID
	 * @return
	 */
	public Long getCmdId();
	/**
	 * ������Ϣ���ȼ���
	 * @return
	 */
	int getPriority();
	void setPriority(int priority);
	
	byte[] getRawPacket();
	String getRawPacketString();
	
	/**
	 * �ж��Ƿ���������(�����������Ӧ��)
	 * @return
	 */
	boolean isHeartbeat();
}
