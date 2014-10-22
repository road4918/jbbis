/**
 * 封装所有GPRS网关客户端通道模块。
 * 由于上行报文需要进行处理，如告警自动应答、发送失败转短信通道、终端通道管理、流量统计、写报文日志等，
 * 需要把收到GPRS报文事件放入到优先级队列进行处理。
 * 封装所有短信客户端通道模块。
 * 
 * 设计要点：由于上行报文，都是有数据接收线程触发调用，因此在设计事件处理机制时，不需要例外启动事件处理线程。
 */
package com.hzjbbis.fk.fe;

import java.util.ArrayList;
import java.util.List;

import com.hzjbbis.fk.clientmod.ClientModule;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.abstra.BaseModule;
import com.hzjbbis.fk.fe.ums.UmsModule;

/**
 * @author bhw
 * 
 */
public class GateClientManage extends BaseModule{
	//可配置属性，通过SPRING配置。
	private List<ClientModule> gprsGateClients = new ArrayList<ClientModule>();
	//还需要SMS通道配置
	private List<UmsModule> umsClients = new ArrayList<UmsModule>();
	
	private static final GateClientManage instance = new GateClientManage();
	
	public static final GateClientManage getInstance(){
		return instance;
	}
	
	private GateClientManage(){}
	
	public void setGprsGateClients(List<ClientModule>gprsGates){
		gprsGateClients = gprsGates;
		for( ClientModule gate: gprsGateClients ){
			gate.init();
			ChannelManage.getInstance().addGprsClient(gate);
		}
	}

	public final void setUmsClients(List<UmsModule> clients) {
		this.umsClients = clients;
		for( UmsModule ums: umsClients ){
			ChannelManage.getInstance().addUmsClient(ums);
		}
	}

	@Override
	public boolean start() {
		return false;
	}

	@Override
	public void stop() {
	}

	public String getModuleType() {
		return IModule.MODULE_TYPE_CONTAINER;
	}

	public final List<ClientModule> getGprsGateClients() {
		return gprsGateClients;
	}

	public final List<UmsModule> getUmsClients() {
		return umsClients;
	}
	
}
