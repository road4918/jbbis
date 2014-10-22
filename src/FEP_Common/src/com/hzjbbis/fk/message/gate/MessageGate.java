/**
 * 网关消息
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
	private String peerAddr;	//对方的IP:PORT地址。对于网关来说，没有实际意义。
	private String txfs;
	private IChannel source;
	protected GateHead head = new GateHead();
	protected ByteBuffer data = emptyData;
	private ByteBuffer rawPacket=null;				//网关原始报文
	//对象内部属性
	private int state = IMessage.STATE_INVALID;		//辅助消息对象的完整读取或者发送
	private int priority = IMessage.PRIORITY_LOW;	//low priority
	//网关消息一般包含一个浙江规约消息对象（终端上行、前置机命令下行）
	private MessageZj innerMessage = null;
	private String serverAddress;

	/** 内部通信：心跳，数据区为每个心跳携带的允许批量发送报文的数量 */
	public static final short CMD_WRAP_ZJ = 0x00;	//默认情况下，读取一个浙江规约消息。
	public static final short CMD_GATE_HREQ = 0x11;
	/** 内部通信：心跳的应答报文 */
	public static final short CMD_GATE_HREPLY = 0x12;

	/** 前置机(主站)第一次连接网关时的参数传递 */
	public static final short CMD_GATE_PARAMS = 0x20;
	/** 前置机(主站)命令下行 */
	public static final short CMD_GATE_REQUEST = 0x21;
	/** 网关报文上行 */
	public static final short CMD_GATE_REPLY = 0x22;
	
	/**
	 * 收到报文的确认帧。如收到前置机请求，并成功(或者失败)发送给终端;
	 * 前置机收到网关的任务上报的确认。
	 */
	public static final short CMD_GATE_CONFIRM = 0x23;
	
	/**
	 * 网关下行失败，需要把下行报文转发给通信前置机，以便走短信通道再次发送。
	 */
	public static final short CMD_GATE_SENDFAIL = 0x24;
	
	public static final short REQ_MONITOR_RELAY_PROFILE = 0x31;		//通过网关消息，传递profile监控请求
	public static final short REP_MONITOR_RELAY_PROFILE = 0x32;		//通过网关消息，传递profile监控应答
	
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
				log.debug("长度不足读取网关报文头，等会儿继续读取。readBuffer.remaining="+readBuffer.remaining());
			return false;		//长度明显不足，等会儿继续读取
		}
		if( head.getMessageType() != MessageType.MSG_GATE && head.getMessageType() != MessageType.MSG_INVAL ){
			//如果网关规约在消息测试中发现不是网关规约，那么innerMessage一定已经创建。
			boolean ret = innerMessage.read(readBuffer);
			if( ret )
				onReadFinished();
			return ret;
		}
		//未知类型消息，则先检测消息类型。如果是网关规约，则进行读取。
		if( IMessage.STATE_INVALID == state || IMessage.STATE_READ_DONE == state ){	//消息头没有读取
			//先检测是否网关规约消息
			MessageType mtype = head.testMessageType(readBuffer);
			if( mtype == MessageType.MSG_INVAL )		//长度太短或者其它原因
				return false;
			if( mtype == MessageType.MSG_ZJ ){
				// to do with ZJ message
				innerMessage = new MessageZj();
				boolean ret = innerMessage.read(readBuffer);
				if( ret ){
					//只是纯粹浙江规约消息
					onReadFinished();
					rawPacket = HexDump.toByteBuffer(innerMessage.getRawPacketString());
				}
				return ret;
			}
			//gate message now 下面针对网关消息进行处理。
			if( readBuffer.remaining()<13 ){
				if( log.isDebugEnabled() )
					log.debug("网关对报文进行分析后，长度不足以读取网关报文头。readBuffer.remaining="+readBuffer.remaining());
				return false;
			}
			state = IMessage.STATE_READ_HEAD;
			boolean ret = head.read(readBuffer);
			if( !ret )
				return false;
			state = IMessage.STATE_READ_DATA;		//开始读数据。
			return readDataSection(readBuffer);
		}
		else if( IMessage.STATE_READ_HEAD == state ){
			boolean ret = head.read(readBuffer);
			if( !ret )
				return false;
			state = IMessage.STATE_READ_DATA;		//开始读数据。
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
				log.debug("readDataSection，长度不足。网关数据区还缺的数据长度="+data.remaining());
			return false;
		}
		buffer.position(buffer.limit());
		if( log.isInfoEnabled() )
			log.info("readDataSection,非法状态，把数据全部清空。");
		return false;	//非法状态，把数据全部清空
	}

	/**
	 * 当网关消息读取完毕时，触发调用该方法。
	 */
	private void onReadFinished() throws MessageParseException{
		final MessageType msgType = head.getMessageType();
		if( msgType == MessageType.MSG_ZJ ){
			//通道上行报文是浙江规约消息。
			if( innerMessage.getIoTime() == 0 )
				innerMessage.setIoTime(System.currentTimeMillis());
			String peer = innerMessage.getPeerAddr();
			if( null == peer )
				innerMessage.setPeerAddr("");
			innerMessage.setSource(this.getSource());
		}
		if( msgType == MessageType.MSG_GATE ){
			if( head.getCommand() == MessageGate.CMD_GATE_REPLY ){
				//从网关上行到主站前置机的报文
				innerMessage = new MessageZj();
				innerMessage.read(data);
				data.rewind();
				if( innerMessage.getIoTime() == 0 ){
					//浙江规约报文未带iotime等属性，是老版本网关格式。
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
				//通过网关的下行命令。
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
	 * 把网关消息写入缓冲区。特别注意，在网关消息发送过程中，如果调用write来获取rawPacket，可能错误。
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
		if( IMessage.STATE_INVALID == state ){	//先发送报文头
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
		//不应该出现的状态。
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
			//缓冲区可写长度小于数据长度，因此逐个写
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
	 * 返回GateMessage所包含的内部消息对象，如网关MessageZj上行消息
	 * @return
	 */
	public MessageZj getInnerMessage() {
		return innerMessage;
	}

	/**
	 * 设置要下行的消息，如主站下行命令消息。前置机向网关方向发送时调用。
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
	 * 设置上行的消息，如终端应答消息或主动上报消息。网关向主站前置机方向发送时调用。
	 */
	public void setUpInnerMessage( IMessage innerMessage) {
		this.innerMessage = (MessageZj)innerMessage;
		head.setCommand(MessageGate.CMD_GATE_REPLY);
		//设置终端地址属性
		if( null != innerMessage.getServerAddress() )
			head.setAttribute(GateHead.ATT_SERVERADDR, innerMessage.getServerAddress());	//终端目的地址
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
	 * 把N个浙江规约报文加载到客户端请求的应答中。
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
