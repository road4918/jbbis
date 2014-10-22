/**
 * 简单消息对象类型
 */
package com.hzjbbis.fk.sockserver.message;

import java.nio.ByteBuffer;

import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.sockserver.AsyncSocketClient;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bao
 *
 */
public class SimpleMessage implements IMessage {
	private MessageType type = MessageType.MSG_SAMPLE;
	private AsyncSocketClient client;
	private int priority = IMessage.PRIORITY_LOW;
	private byte[] input,output;
	private int offset = 0; 
	private long ioTime;		//完整收到消息或者发送完毕时间
	private String peerAddr;	//对方的IP:PORT地址
	private String serverAddress;
	private String txfs="";

	public MessageType getMessageType() {
		return type;
	}

	public boolean read(ByteBuffer readBuffer) {
		if( !readBuffer.hasRemaining() )
			return false;
		input = new byte[readBuffer.remaining()];
		readBuffer.get(input);
		return true;
	}

	public boolean write(ByteBuffer writeBuffer) {
		if( null == output || 0==output.length )
			return true;	//写完成。
		int minLength = Math.min(output.length-offset, writeBuffer.remaining());
		writeBuffer.put(output, offset, minLength);
		offset += minLength;
		return offset == output.length;
	}

	public byte[] getOutput() {
		return output;
	}

	public void setOutput(byte[] output) {
		this.output = output;
	}

	public byte[] getInput() {
		return input;
	}

	public IChannel getSource() {
		return client;
	}

	public void setSource(IChannel src) {
		client = (AsyncSocketClient)src;
	}
	
	public long getIoTime() {
		return ioTime;
	}

	public void setIoTime(long ioTime) {
		this.ioTime = ioTime;
	}

	public String getPeerAddr() {
		return peerAddr;
	}

	public void setPeerAddr(String peerAddr) {
		this.peerAddr = peerAddr;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer(1024);
		boolean empty = true;
		if( null !=output ){
			sb.append("下行消息:");
			sb.append(HexDump.hexDumpCompact(output,0,output.length));
			empty = false;
		}
		else if( null!= input ){
			sb.append("上行消息:");
			sb.append(HexDump.hexDumpCompact(input,0,input.length));
//			sb.append("##");
			empty = false;
		}
		if( empty )
			sb.append("空消息");
		return sb.toString();
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		if( priority> IMessage.PRIORITY_MAX )
			priority = IMessage.PRIORITY_MAX;
		else if( priority< IMessage.PRIORITY_LOW )
			priority = IMessage.PRIORITY_LOW;
		this.priority = priority;
	}
	

	public String getRawPacketString() {
		return HexDump.hexDumpCompact(input, 0, input.length);
	}
	
	public byte[] getRawPacket() {
		return input;
	}

	public String getTxfs() {
		return txfs;
	}

	public void setTxfs(String fs) {
		txfs = fs;
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
		return false;
	}
}
