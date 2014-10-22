package com.hzjbbis.fk.fe.ums;

import java.net.SocketAddress;

import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseClientChannel;

/**
 * 浙江电力的UMS短信网关客户端连接模块的基类。
 * @author bhw
 * 2008-10-20
 */
public abstract class BaseClientChannelModule extends BaseClientChannel implements IModule {
	//可配置属性
	protected String peerIp;			//client所连接的服务器IP
	protected int peerPort=0;			//服务器端口
	protected String txfs="01";			//短信通道模块
	protected String name="UMS通道";

	//统计属性,初始化自动为0
	protected long lastReceiveTime=0;							//最新接收报文时间
	protected long lastSendTime = 0;							//最近发送成功的时间
	protected long totalRecvMessages=0,totalSendMessages=0;		//总共收、发消息总数
	protected int msgRecvPerMinute=0,msgSendPerMinute=0;		//每分钟收、发报文个数
	protected String moduleType = IModule.MODULE_TYPE_UMS_CLIENT;

	public String getPeerAddr() {
		return peerIp+":"+peerPort;
	}

	public String getPeerIp() {
		return peerIp;
	}
	
	public void setPeerIp(String peerIp){
		this.peerIp = peerIp;
	}
	
	/**
	 * 服务器设置,可以使用peerIp/hostIp; peerPort/hostPort
	 * @param hostIp
	 */
	public void setHostIp(String hostIp){
		peerIp = hostIp;
	}

	public int getPeerPort() {
		return peerPort;
	}
	
	public void setPeerPort(int peerPort){
		this.peerPort = peerPort;
	}
	
	public void setHostPort(int hostPort){
		peerPort = hostPort;
	}

	public SocketAddress getSocketAddress() {	//UDP服务器管理client对象使用。这里不需要.
		return null;
	}

	public String getModuleType() {
		return moduleType;
	}

	public void setModuleType(String type ){
		moduleType = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name ){
		this.name = name;
	}

	public String getTxfs() {
		return txfs;
	}
	
	public void setTxfs(String txfs){
		this.txfs = txfs;
	}

	public long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public long getLastSendTime() {
		return lastSendTime;
	}

	public int getMsgRecvPerMinute() {
		return this.msgRecvPerMinute;
	}

	public int getMsgSendPerMinute() {
		return this.msgSendPerMinute;
	}

	public long getTotalRecvMessages() {
		return totalRecvMessages;
	}

	public long getTotalSendMessages() {
		return totalSendMessages;
	}

	public String profile() {
		return "";
	}

}
