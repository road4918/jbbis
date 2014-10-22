/**
 * UDPͨ��client �ࡣUDP������һ�����ͬ��д���ݡ�
 */
package com.hzjbbis.fk.sockserver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 * 2008-06-05 12:58
 */
public class UdpClient implements IServerSideChannel{
	private SocketAddress socketAddress;
	private SyncUdpServer server;
	private String peerIp = "0.0.0.0";
	private int peerPort = 0;
	//IO֧������
	private ByteBuffer bufRead = null;		//֧�ָ��ع�Լ����
	private IMessage curReadingMsg;		//��ǰ���ڶ�д����Ϣ��
	//״̬����
	private long lastIoTime = System.currentTimeMillis();
	private long lastReadTime = System.currentTimeMillis();
	private boolean bufferHasRemaining = false;
	//�������ȿ������ԡ���¼�����¼�����������
	private int lastingWrite = 0;

	//�ͻ���������������͵ı�������
	private int requestNum = -1;

	public int getRequestNum() {
		return requestNum;
	}

	public void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}

	public int getLastingWrite() {
		return lastingWrite;
	}

	public void setLastingWrite(int lastingWrite) {
		this.lastingWrite = lastingWrite;
	}

	public UdpClient(SocketAddress sa,SyncUdpServer udpServer){
		socketAddress = sa;
		server = udpServer;
		bufRead = ByteBuffer.allocate(server.getBufLength());
		if( sa instanceof InetSocketAddress){
			peerIp = ((InetSocketAddress)sa).getAddress().getHostAddress();
			peerPort = ((InetSocketAddress)sa).getPort();
		}else{
			String connstr = sa.toString();
			if(connstr!=null){
				if(connstr.charAt(0)=='/'){
					connstr=connstr.substring(1);
				}
				String[] parts = connstr.split(":");
				if( parts.length>=2 ){
					peerIp = parts[0];
					peerPort = Integer.parseInt(parts[1]);
				}
			}
		}
	}

	public boolean send(IMessage msg) {
		return server.send(msg, this);
	}
	
	public int sendQueueSize(){
		return 0;
	}
	
	public void setMaxSendQueueSize(int maxSize){
		
	}
	
	public IMessage getCurReadingMsg() {
		return curReadingMsg;
	}

	public void setCurReadingMsg(IMessage curReadingMsg) {
		this.curReadingMsg = curReadingMsg;
	}

	public SyncUdpServer getServer() {
		return server;
	}

	public String getPeerIp() {
		return peerIp;
	}

	public int getPeerPort() {
		return peerPort;
	}
	
	public String getPeerAddr(){
		return peerIp+ ":" + peerPort + ":U";	//��ʾUDP��ַ
	}

	public ByteBuffer getBufRead() {
		return bufRead;
	}

	public SocketChannel getChannel(){
		return null;
	}
	public void setIoThread(Object ioThread) {
	}
	
	public SocketAddress getSocketAddress() {
		return socketAddress;
	}
	
	public long getLastIoTime(){
		return lastIoTime;
	}
	
	public long getLastReadTime(){
		return lastReadTime;
	}
	
	public void setLastIoTime(){
		lastIoTime = System.currentTimeMillis();
	}
	
	public void setLastReadTime(){
		lastReadTime = System.currentTimeMillis();
		lastIoTime = lastReadTime;
	}
	
	//UDP client can not be closed
	public void close() {}

	public ByteBuffer getBufWrite() {
		return null;
	}

	public IMessage getCurWritingMsg() {
		return null;
	}

	public IMessage getNewSendMessage() {
		return null;
	}

	public void setCurWritingMsg(IMessage curWritingMsg) {
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("UDP client,peer=").append(peerIp);
		sb.append(":").append(peerPort);
		return sb.toString();
	}
	
	public boolean bufferHasRemaining(){
		return bufferHasRemaining;
	}
	
	public void setBufferHasRemaining(boolean hasRemaining){
		bufferHasRemaining = hasRemaining;
	}
}
