/**
 * ������Ϣ
 */
package com.hzjbbis.fk.message.gate;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.exception.MessageParseException;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author hbao
 *
 */
public class MessageGate implements IMessage {
	private static final Logger log = Logger.getLogger(MessageGate.class);
	private static final byte[] zeroPacket = new byte[0];
	private static final ByteBuffer emptyData = ByteBuffer.wrap(zeroPacket);
	protected MessageType type = MessageType.MSG_GATE;
	private long ioTime = System.currentTimeMillis();
	private String peerAddr;	//�Է���IP:PORT��ַ������������˵��û��ʵ�����塣
	private String txfs;
	private IChannel source;
	protected GateHead head = new GateHead();
	protected ByteBuffer data = emptyData;
	private ByteBuffer rawPacket=null;				//����ԭʼ����
	//�����ڲ�����
	private int state = IMessage.STATE_INVALID;		//������Ϣ�����������ȡ���߷���
	private int priority = IMessage.PRIORITY_LOW;	//low priority
	//������Ϣһ�����һ���㽭��Լ��Ϣ�����ն����С�ǰ�û��������У�
	private MessageZj innerMessage = null;
	private String serverAddress;

	/** �ڲ�ͨ�ţ�������������Ϊÿ������Я���������������ͱ��ĵ����� */
	public static final short CMD_WRAP_ZJ = 0x00;	//Ĭ������£���ȡһ���㽭��Լ��Ϣ��
	public static final short CMD_GATE_HREQ = 0x11;
	/** �ڲ�ͨ�ţ�������Ӧ���� */
	public static final short CMD_GATE_HREPLY = 0x12;

	/** ǰ�û�(��վ)��һ����������ʱ�Ĳ������� */
	public static final short CMD_GATE_PARAMS = 0x20;
	/** ǰ�û�(��վ)�������� */
	public static final short CMD_GATE_REQUEST = 0x21;
	/** ���ر������� */
	public static final short CMD_GATE_REPLY = 0x22;
	
	/**
	 * �յ����ĵ�ȷ��֡�����յ�ǰ�û����󣬲��ɹ�(����ʧ��)���͸��ն�;
	 * ǰ�û��յ����ص������ϱ���ȷ�ϡ�
	 */
	public static final short CMD_GATE_CONFIRM = 0x23;
	
	/**
	 * ��������ʧ�ܣ���Ҫ�����б���ת����ͨ��ǰ�û����Ա��߶���ͨ���ٴη��͡�
	 */
	public static final short CMD_GATE_SENDFAIL = 0x24;
	
	public static final short REQ_MONITOR_RELAY_PROFILE = 0x31;		//ͨ��������Ϣ������profile�������
	public static final short REP_MONITOR_RELAY_PROFILE = 0x32;		//ͨ��������Ϣ������profile���Ӧ��
	
	public long getIoTime() {
		return ioTime;
	}

	public MessageType getMessageType() {
		return type;
	}

	public String getPeerAddr() {
		return peerAddr;
	}

	public int getPriority() {
		return priority;
	}

	public byte[] getRawPacket() {
		if( null != rawPacket )
			return rawPacket.array();
		else
			return zeroPacket;
	}

	public String getRawPacketString() {
		if( null != rawPacket )
			return HexDump.hexDumpCompact(rawPacket);
		else
			return "";
	}

	public IChannel getSource() {
		return source;
	}

	public boolean read(ByteBuffer readBuffer) throws MessageParseException{
		synchronized(this){
			return _read(readBuffer);
		}
	}
	
	public boolean _read(ByteBuffer readBuffer) throws MessageParseException{
		if( state == IMessage.STATE_INVALID && readBuffer.remaining()<13 ){
			if( log.isDebugEnabled() )
				log.debug("���Ȳ����ȡ���ر���ͷ���Ȼ��������ȡ��readBuffer.remaining="+readBuffer.remaining());
			return false;		//�������Բ��㣬�Ȼ��������ȡ
		}
		if( head.getMessageType() != MessageType.MSG_GATE && head.getMessageType() != MessageType.MSG_INVAL ){
			//������ع�Լ����Ϣ�����з��ֲ������ع�Լ����ôinnerMessageһ���Ѿ�������
			boolean ret = innerMessage.read(readBuffer);
			if( ret )
				onReadFinished();
			return ret;
		}
		//δ֪������Ϣ�����ȼ����Ϣ���͡���������ع�Լ������ж�ȡ��
		if( IMessage.STATE_INVALID == state || IMessage.STATE_READ_DONE == state ){	//��Ϣͷû�ж�ȡ
			//�ȼ���Ƿ����ع�Լ��Ϣ
			MessageType mtype = head.testMessageType(readBuffer);
			if( mtype == MessageType.MSG_INVAL )		//����̫�̻�������ԭ��
				return false;
			if( mtype == MessageType.MSG_ZJ ){
				// to do with ZJ message
				innerMessage = new MessageZj();
				boolean ret = innerMessage.read(readBuffer);
				if( ret ){
					//ֻ�Ǵ����㽭��Լ��Ϣ
					onReadFinished();
					rawPacket = HexDump.toByteBuffer(innerMessage.getRawPacketString());
				}
				return ret;
			}
			//gate message now �������������Ϣ���д���
			if( readBuffer.remaining()<13 ){
				if( log.isDebugEnabled() )
					log.debug("���ضԱ��Ľ��з����󣬳��Ȳ����Զ�ȡ���ر���ͷ��readBuffer.remaining="+readBuffer.remaining());
				return false;
			}
			state = IMessage.STATE_READ_HEAD;
			boolean ret = head.read(readBuffer);
			if( !ret )
				return false;
			state = IMessage.STATE_READ_DATA;		//��ʼ�����ݡ�
			return readDataSection(readBuffer);
		}
		else if( IMessage.STATE_READ_HEAD == state ){
			boolean ret = head.read(readBuffer);
			if( !ret )
				return false;
			state = IMessage.STATE_READ_DATA;		//��ʼ�����ݡ�
			return readDataSection(readBuffer);
		}
		else if( IMessage.STATE_READ_DATA == state )
			return readDataSection(readBuffer);
		else
			return true;
	}
	
	private boolean readDataSection(ByteBuffer buffer) throws MessageParseException{
		if( state == IMessage.STATE_READ_DATA ){
			if( emptyData == data && head.getIntBodylen()>0 ){
				data = ByteBuffer.wrap(new byte[head.getIntBodylen()]);
			}
			if( data.remaining()>= buffer.remaining() )
				data.put(buffer);
			else{
				buffer.get(data.array(),data.position(),data.remaining());
				data.position(data.limit());
			}
			if( data.remaining() == 0 ){
				data.flip();
				rawPacket = ByteBuffer.allocate(data.remaining()+head.getHeadLen());
				rawPacket.put(head.getRawHead()).put(data);
				rawPacket.rewind();
				data.rewind();
				state = IMessage.STATE_READ_DONE;
				ioTime = System.currentTimeMillis();
				onReadFinished();
				return true;
			}
			if( log.isDebugEnabled() )
				log.debug("readDataSection�����Ȳ��㡣������������ȱ�����ݳ���="+data.remaining());
			return false;
		}
		buffer.position(buffer.limit());
		if( log.isInfoEnabled() )
			log.info("readDataSection,�Ƿ�״̬��������ȫ����ա�");
		return false;	//�Ƿ�״̬��������ȫ�����
	}

	/**
	 * ��������Ϣ��ȡ���ʱ���������ø÷�����
	 */
	private void onReadFinished() throws MessageParseException{
		final MessageType msgType = head.getMessageType();
		if( msgType == MessageType.MSG_ZJ ){
			//ͨ�����б������㽭��Լ��Ϣ��
			if( innerMessage.getIoTime() == 0 )
				innerMessage.setIoTime(System.currentTimeMillis());
			String peer = innerMessage.getPeerAddr();
			if( null == peer )
				innerMessage.setPeerAddr("");
			innerMessage.setSource(this.getSource());
		}
		if( msgType == MessageType.MSG_GATE ){
			if( head.getCommand() == MessageGate.CMD_GATE_REPLY ){
				//���������е���վǰ�û��ı���
				innerMessage = new MessageZj();
				innerMessage.read(data);
				data.rewind();
				if( innerMessage.getIoTime() == 0 ){
					//�㽭��Լ����δ��iotime�����ԣ����ϰ汾���ظ�ʽ��
					innerMessage.setIoTime(System.currentTimeMillis());
					String peer = head.getAttributeAsString(GateHead.ATT_DESTADDR); 
					if( peer.length() == 0 )
						innerMessage.setPeerAddr(source.toString());
					else
						innerMessage.setPeerAddr(peer);
				}
				String _txfs = head.getAttributeAsString(GateHead.ATT_TXFS);
				if( _txfs.length()!=0 )
					innerMessage.setTxfs(_txfs);
				String serverAddress = head.getAttributeAsString(GateHead.ATT_SERVERADDR);
				if( serverAddress.length()>0 ){
					setServerAddress(serverAddress);
					innerMessage.setServerAddress(serverAddress);
				}
			}
			else if( head.getCommand() == MessageGate.CMD_GATE_REQUEST ){
				//ͨ�����ص��������
				innerMessage = new MessageZj();
				innerMessage.read(data);
				data.rewind();
			}
			if( null != innerMessage )
				innerMessage.setSource(this.getSource());
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
		if( null != this.innerMessage )
			this.innerMessage.setSource(src);
	}

	/**
	 * ��������Ϣд�뻺�������ر�ע�⣬��������Ϣ���͹����У��������write����ȡrawPacket�����ܴ���
	 */
	public boolean write(ByteBuffer writeBuffer){
		synchronized(this){
			return _write(writeBuffer);
		}
	}
	
	private boolean _write(ByteBuffer writeBuffer) {
		if( IMessage.STATE_SEND_DONE == state )
			return true;
		if( IMessage.STATE_READ_DONE == state )
			state = IMessage.STATE_INVALID;
		if( IMessage.STATE_INVALID == state ){	//�ȷ��ͱ���ͷ
			if( null == data )
				head.setIntBodylen(0);
			else
				head.setIntBodylen(data.remaining());
			state = IMessage.STATE_SEND_HEAD;
			if( ! head.write(writeBuffer) )
				return false;
			state = IMessage.STATE_SEND_DATA;
			return _writeDataSection(writeBuffer);
		}
		else if( IMessage.STATE_SEND_HEAD == state ){
			if( ! head.write(writeBuffer) )
				return false;
			state = IMessage.STATE_SEND_DATA;
			return _writeDataSection(writeBuffer);
		}
		else if( IMessage.STATE_SEND_DATA == state )
			return _writeDataSection(writeBuffer);
		//��Ӧ�ó��ֵ�״̬��
		return true;
	}
	
	private boolean _writeDataSection(ByteBuffer buffer){
		if( buffer.remaining()>= data.remaining() ){
			buffer.put(data);
			data.rewind();
			ioTime = System.currentTimeMillis();
			rawPacket = ByteBuffer.allocate(head.getHeadLen()+data.remaining());
			rawPacket.put(head.getRawHead()).put(data);
			data.rewind();	rawPacket.flip();
			state = IMessage.STATE_INVALID;
			return true;
		}
		else{
			//��������д����С�����ݳ��ȣ�������д
			int limit = data.limit();
			data.limit(data.position()+buffer.remaining());
			buffer.put(data);
			data.limit(limit);
			return false;
		}
	}

	public String getTxfs() {
		return txfs;
	}

	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}

	/**
	 * ����GateMessage���������ڲ���Ϣ����������MessageZj������Ϣ
	 * @return
	 */
	public MessageZj getInnerMessage() {
		return innerMessage;
	}

	/**
	 * ����Ҫ���е���Ϣ������վ����������Ϣ��ǰ�û������ط�����ʱ���á�
	 */
	public void setDownInnerMessage(MessageZj innerMessage) {
		this.innerMessage = innerMessage;
		this.innerMessage.setSource(this.getSource());
		head.setCommand(MessageGate.CMD_GATE_REQUEST);
		data = ByteBuffer.wrap(innerMessage.getRawPacket());
		head.setIntBodylen(data.remaining());
		String innerMsg = innerMessage.getRawPacketString();
		rawPacket = ByteBuffer.allocate(head.getHeadLen()+innerMsg.length()/2);
		rawPacket.put(head.getRawHead()).put(HexDump.toByteBuffer(innerMsg));
		rawPacket.flip();
	}

	/**
	 * �������е���Ϣ�����ն�Ӧ����Ϣ�������ϱ���Ϣ����������վǰ�û�������ʱ���á�
	 */
	public void setUpInnerMessage( IMessage innerMessage) {
		this.innerMessage = (MessageZj)innerMessage;
		head.setCommand(MessageGate.CMD_GATE_REPLY);
		//�����ն˵�ַ����
		if( null != innerMessage.getServerAddress() )
			head.setAttribute(GateHead.ATT_SERVERADDR, innerMessage.getServerAddress());	//�ն�Ŀ�ĵ�ַ
//		head.setAttribute(GateHead.ATT_TXSJ, innerMessage.getIoTime());
//		head.setAttribute(GateHead.ATT_TXFS, innerMessage.getTxfs());
		data = ByteBuffer.wrap(innerMessage.getRawPacket());
		head.setIntBodylen(data.remaining());
		String innerMsg = innerMessage.getRawPacketString();
		rawPacket = ByteBuffer.allocate(head.getHeadLen()+innerMsg.length()/2);
		rawPacket.put(head.getRawHead()).put(HexDump.toByteBuffer(innerMsg));
		rawPacket.flip();
	}

	public GateHead getHead() {
		return head;
	}
	
	public ByteBuffer getData() {
		return data;
	}
	
	public void setData(ByteBuffer data){
		this.data = data;
	}
	
	@Override
	public String toString() {
		return getRawPacketString();
	}

	public static MessageGate createHRequest(int numPackets ){
		MessageGate msg = new MessageGate();
		msg.head.setCommand(MessageGate.CMD_GATE_HREQ);
		msg.data = ByteBuffer.allocate(8);
		msg.data.putInt(numPackets).flip();
		return msg;
	}
	
	public static MessageGate createHReply(){
		MessageGate msg = new MessageGate();
		msg.head.setCommand(MessageGate.CMD_GATE_HREPLY);
		return msg;
	}
	
	public static final MessageGate createMoniteProfileRequest(){
		MessageGate msg = new MessageGate();
		msg.head.setCommand(MessageGate.REQ_MONITOR_RELAY_PROFILE);
		return msg;
	}
	
	public static final MessageGate createMoniteProfileReply(String profile){
		MessageGate msg = new MessageGate();
		msg.head.setCommand(MessageGate.REP_MONITOR_RELAY_PROFILE);
		msg.setPriority(IMessage.PRIORITY_VIP);
		if( null != profile && profile.length()>0 ){
			byte[] bts = profile.getBytes();
			msg.data = ByteBuffer.wrap(bts);
		}
		return msg;
	}
	
	/**
	 * ��N���㽭��Լ���ļ��ص��ͻ��������Ӧ���С�
	 * @param carriedMsgs
	 * @return
	 */
	public static MessageGate createHReply(ByteBuffer carriedMsgs ){
		MessageGate msg = new MessageGate();
		msg.head.setCommand(MessageGate.CMD_GATE_HREPLY);
		msg.data = carriedMsgs;
		return msg;
	}

	public Long getCmdId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public boolean isHeartbeat() {
		return head.getCommand() == MessageGate.CMD_GATE_HREPLY || head.getCommand() == MessageGate.CMD_GATE_HREQ;
	}

}
