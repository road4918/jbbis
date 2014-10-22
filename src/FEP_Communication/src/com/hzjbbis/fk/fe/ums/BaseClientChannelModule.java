package com.hzjbbis.fk.fe.ums;

import java.net.SocketAddress;

import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseClientChannel;

/**
 * �㽭������UMS�������ؿͻ�������ģ��Ļ��ࡣ
 * @author bhw
 * 2008-10-20
 */
public abstract class BaseClientChannelModule extends BaseClientChannel implements IModule {
	//����������
	protected String peerIp;			//client�����ӵķ�����IP
	protected int peerPort=0;			//�������˿�
	protected String txfs="01";			//����ͨ��ģ��
	protected String name="UMSͨ��";

	//ͳ������,��ʼ���Զ�Ϊ0
	protected long lastReceiveTime=0;							//���½��ձ���ʱ��
	protected long lastSendTime = 0;							//������ͳɹ���ʱ��
	protected long totalRecvMessages=0,totalSendMessages=0;		//�ܹ��ա�����Ϣ����
	protected int msgRecvPerMinute=0,msgSendPerMinute=0;		//ÿ�����ա������ĸ���
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
	 * ����������,����ʹ��peerIp/hostIp; peerPort/hostPort
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

	public SocketAddress getSocketAddress() {	//UDP����������client����ʹ�á����ﲻ��Ҫ.
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
