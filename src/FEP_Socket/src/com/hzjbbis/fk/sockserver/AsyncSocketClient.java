/**
 * SocketServer��������첽SocketClient.
 * ��д��Ϣ(IMessage������ķ�ʽ��
 *    ����IClientIO�ӿ�ʵ�֣����Serverû��ʵ���࣬�����SimpleIoHandler�ࣩ��
 *    �����̣�channel-> bufRead -> IMessage.read(bufRead);
 *    д����: IMessage->client���Ͷ���->֪ͨioThread�� ioThread�ص�IClientIO�ӿڡ�
 */
package com.hzjbbis.fk.sockserver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.sockserver.event.MessageSendFailEvent;
import com.hzjbbis.fk.sockserver.io.SocketIoThread;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 * 2008-05-22
 */
public class AsyncSocketClient implements IServerSideChannel{
	//ר������
	protected SocketChannel channel;
	protected String peerIp; //�Է�IP��ַ
	protected int peerPort;  //�Է�port
	protected String peerAddr;
	protected String localIp;//����IP��ַ
	protected int localPort;
	protected String localAddr; //ip+":"+port;
	
	//IO֧������
	protected ByteBuffer bufRead,bufWrite;
	protected IMessage curReadingMsg,curWritingMsg;		//��ǰ���ڶ�д����Ϣ��
	protected List<IMessage> sendList = new LinkedList<IMessage>();
	
	//�������ȿ������ԡ���¼�����¼�����������
	private int lastingWrite = 0;
	
	//��������
	private static final Logger log = Logger.getLogger(AsyncSocketClient.class);
	protected ISocketServer server;
	protected SocketIoThread ioThread;
	private int intKey = 0;				// ���Է��� RTUA
	private int maxSendQueueSize = 20;	//�첽���ͣ����ȴ����͵ı�������

	//״̬����
	private long lastIoTime = System.currentTimeMillis();
	private long lastReadTime = System.currentTimeMillis();
	private boolean bufferHasRemaining = false;
	
	//�ͻ���������������͵ı�������
	private int requestNum = -1;

	public AsyncSocketClient(){}
	
	public AsyncSocketClient(SocketChannel c,ISocketServer s){
		channel = c;
		server = s;
		try {
			peerIp = channel.socket().getInetAddress().getHostAddress();
			peerPort = channel.socket().getPort();
			peerAddr = peerIp + ":" + peerPort +":T";
			localIp = channel.socket().getLocalAddress().getHostAddress();
			localPort = channel.socket().getLocalPort();
			localAddr = localIp + ":"
					+ HexDump.toHex((short)localPort);
		} catch (Exception e) {	}
		bufRead = ByteBuffer.allocateDirect(s.getBufLength());
		bufWrite = ByteBuffer.allocateDirect(s.getBufLength());
	}

	public boolean send(IMessage msg){
		if( sendList.size()>= this.maxSendQueueSize ){
			log.warn(toString()+"-���Ͷ��г���>maxSendQueueSize������Ϣ������");
			//֪ͨ��Ϣ�����¼���
			GlobalEventHandler.postEvent(new MessageSendFailEvent(msg,this));
			return false;
		}
		synchronized(sendList){
			if( this.requestNum>0 ){
				synchronized(this){
					this.requestNum--;
				}
			}
			sendList.add(msg);
		}
		ioThread.clientWriteRequest(this);
		return true;
	}
	
	public int sendQueueSize(){
		synchronized(sendList){
			return sendList.size();
		}
	}
	
	public void setMaxSendQueueSize(int maxSendQueueSize){
		this.maxSendQueueSize = maxSendQueueSize;
	}
	
	public IMessage getNewSendMessage(){
		synchronized(sendList){
			if( sendList.size() == 0 )
				return null;
			return sendList.remove(0);
		}
	}

	/**
	 * �ر�socketChannel ����
	 */
	public void close(){
		try {
			channel.socket().shutdownInput();
			channel.socket().shutdownOutput();
		} catch (Exception exp) {
		}
		try {
			channel.close();
			channel = null;
			
		} catch (Exception exp) {
		}
		if( log.isInfoEnabled() ){
			log.info("�ͻ��˹ر�["+peerIp+":"+peerPort+",localport:"+localPort+"]");
		}
		//δ���͵���Ϣ֪ͨ
		synchronized(sendList){
			for(IMessage msg: sendList){
				GlobalEventHandler.postEvent(new MessageSendFailEvent(msg,this));
			}
			sendList.clear();
		}
	}
	
	public SocketChannel getChannel() {
		return channel;
	}
	
	public SocketAddress getSocketAddress(){
		return channel.socket().getRemoteSocketAddress();
	}
	
	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public String getPeerIp() {
		return peerIp;
	}

	public void setPeerIp(String peerIp) {
		this.peerIp = peerIp;
	}

	public int getPeerPort() {
		return peerPort;
	}

	public void setPeerPort(int peerPort) {
		this.peerPort = peerPort;
	}

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public String getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}

	public final SocketIoThread getIoThread() {
		return ioThread;
	}

	public void setIoThread(Object ioThread) {
		this.ioThread = (SocketIoThread)ioThread;
	}

	public ISocketServer getServer() {
		return server;
	}

	public void setServer(TcpSocketServer server) {
		this.server = server;
	}
	
	public void closeRequest(){
		ioThread.closeClientRequest(this);
	}

	public IMessage getCurReadingMsg() {
		return curReadingMsg;
	}

	public void setCurReadingMsg(IMessage curReadingMsg) {
		this.curReadingMsg = curReadingMsg;
		if( null != curReadingMsg )
			server.incRecvMessage();			//���յ�ÿ�����ģ�����������������
	}

	public IMessage getCurWritingMsg() {
		return curWritingMsg;
	}

	public void setCurWritingMsg(IMessage curWritingMsg) {
		this.curWritingMsg = curWritingMsg;
		if( null != curWritingMsg )
			server.incSendMessage();
	}

	public ByteBuffer getBufRead() {
		return bufRead;
	}

	public ByteBuffer getBufWrite() {
		return bufWrite;
	}
	
	public String toString(){
		return this.peerAddr;
	}

	public int getIntKey() {
		return intKey;
	}

	public void setIntKey(int intKey) {
		this.intKey = intKey;
	}

	public final String getPeerAddr() {
		return peerAddr;
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
	
	public int getLastingWrite() {
		return lastingWrite;
	}

	public void setLastingWrite(int lastingWrite) {
		this.lastingWrite = lastingWrite;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public int getRequestNum() {
		return requestNum;
	}

	public void setRequestNum(int requestNum) {
		synchronized(this){
			this.requestNum = requestNum;
		}
	}
	
	public boolean bufferHasRemaining(){
		return bufferHasRemaining;
	}
	
	public void setBufferHasRemaining(boolean hasRemaining){
		bufferHasRemaining = hasRemaining;
	}
}
