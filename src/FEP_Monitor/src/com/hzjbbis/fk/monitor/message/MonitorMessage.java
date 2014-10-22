package com.hzjbbis.fk.monitor.message;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.exception.SocketClientCloseException;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.monitor.MonitorCommand;

/**
 * ������ģ�����Ϣ����
 * @author hbao
 * Э���ʽ��
 * 9�ֽڱ��ı�־[JBMONITOR]+2�ֽ�������+4�ֽ��������������ݳ��ȣ� + �������ݡ�
 */
public class MonitorMessage implements IMessage {
	//��̬����
	private static final Logger log = Logger.getLogger(MonitorMessage.class);
	private static final MessageType msgType = MessageType.MSG_MONITOR;
	private static final byte[] FLAG = "JBMONITOR".getBytes();
	private static final ByteBuffer voidBody = ByteBuffer.allocate(0);
	//��Ϣ��������
	private long ioTime = System.currentTimeMillis();	//�Լ�ع���ģ�飬ioTimeû�����á� ***
	private String peerAddr;	//�Է���IP:PORT��ַ�����ڼ�ع�����˵��û��ʵ�����塣		  ***
	private String serverAddress;
	private String txfs = "tcp";	//�Լ�ع���û�����塣								  ***
	private IChannel source;
	private int priority = IMessage.PRIORITY_LOW;	//low priority					  ***
	private short command = MonitorCommand.CMD_INVALID;
	private int bodyLen = 0;						//�����峤��
	private ByteBuffer body = voidBody;					//������
	//�ڲ���������
	private int state = IMessage.STATE_INVALID;		//������Ϣ�����������ȡ���߷���

	public long getIoTime() {
		return ioTime;
	}

	public MessageType getMessageType() {
		return msgType;
	}

	public String getPeerAddr() {
		return peerAddr;
	}

	public int getPriority() {
		return priority;
	}

	public byte[] getRawPacket() {
		return body.array();
	}

	public String getRawPacketString() {
		return "un implemented";
	}

	public IChannel getSource() {
		return source;
	}

	public String getTxfs() {
		return txfs;
	}

	public boolean read(ByteBuffer readBuffer) {
		if( state == IMessage.STATE_INVALID && readBuffer.remaining()<15 ){
			if( log.isDebugEnabled() )
				log.debug("���Ȳ����ȡ��ر���ͷ���Ȼ��������ȡ��readBuffer.remaining="+readBuffer.remaining());
			return false;		//�������Բ��㣬�Ȼ��������ȡ
		}
		if( state == IMessage.STATE_INVALID ){	//��Ϣͷû�ж�ȡ
			for(int i=0; i<FLAG.length; i++ ){
				if( readBuffer.get() != FLAG[i] )
					throw new SocketClientCloseException("���ı�־��ƥ�䡣����ر�ͨѶ����");
			}
			command = readBuffer.getShort();
			bodyLen = readBuffer.getInt();		//Ĭ�������ֽ�˳��.
			body = ByteBuffer.allocate(bodyLen);
			state = IMessage.STATE_READ_DATA;
			if( readBuffer.remaining()>= body.remaining() ){
				readBuffer.get(body.array());
				state = IMessage.STATE_READ_DONE;
				return true;
			}
			body.put(readBuffer);
			return false;
		}
		else if( state == IMessage.STATE_READ_DATA ){
			if( readBuffer.remaining()>= body.remaining() ){
				readBuffer.get(body.array(),body.position(),body.remaining() );
				body.position(0);		//������ȡ��position��0����ʾ���ݿɶ�״̬
				state = IMessage.STATE_READ_DONE;
				return true;
			}
			body.put(readBuffer);
			return false;
		}
		else{
			log.error("״̬�Ƿ������Ƕ�ȡ��Ϣ״̬���ر�ͨ�����ӡ��������BUG��");
			throw new SocketClientCloseException("״̬�Ƿ������Ƕ�ȡ��Ϣ״̬���ر�ͨ�����ӡ��������BUG��");
		}
	}

	public void setIoTime(long time) {
		ioTime = time;
	}

	public void setPeerAddr(String peer) {
		peerAddr = peer;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setSource(IChannel src) {
		source = src;
	}

	public void setTxfs(String fs) {
		txfs = fs;
	}

	public boolean write(ByteBuffer writeBuffer) {
		if( IMessage.STATE_SEND_DONE == state )
			return true;
		if( IMessage.STATE_INVALID == state ){	//�ȷ��ͱ���ͷ
			if( writeBuffer.remaining()<15 ){
				log.debug("���������Ȳ��㣬���ܷ��ͼ�ر���ͷ��writeBuffer.len="+writeBuffer.remaining());
				return false;
			}
			writeBuffer.put(FLAG);
			writeBuffer.putShort(command);
			writeBuffer.putInt(body.remaining());
			state = IMessage.STATE_SEND_DATA;
			return sendDataSection(writeBuffer);
		}
		else if( IMessage.STATE_SEND_DATA == state )
			return sendDataSection(writeBuffer);
		else{
			log.error("״̬�Ƿ������Ƿ��ͼ����Ϣ״̬���ر�ͨ�����ӡ��������BUG��");
			throw new SocketClientCloseException("״̬�Ƿ������Ƿ��ͼ����Ϣ״̬���ر�ͨ�����ӡ��������BUG��");
		}
	}

	private boolean sendDataSection(ByteBuffer writeBuffer){
		if( writeBuffer.remaining()> body.remaining() ){
			writeBuffer.put(body);
			body.rewind();
			state = IMessage.STATE_SEND_DONE;
			ioTime = System.currentTimeMillis();
			return true;
		}
		//�������������Է���ȫ����body���ݡ�
		int limit = body.limit();
		body.limit(body.position()+writeBuffer.remaining());
		writeBuffer.put(body);
		body.limit(limit);
		return false;
	}

	public short getCommand() {
		return command;
	}

	public void setCommand(short command) {
		this.command = command;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public void setBody(ByteBuffer body) {
		this.body = body;
	}
	
	public void resetMessageState(){
		state = IMessage.STATE_INVALID;
	}
	
	public Long getCmdId() {
		return new Long(0);
	}

	public String getStatus() {
		return "";
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public boolean isHeartbeat() {
		return false;
	}

}
