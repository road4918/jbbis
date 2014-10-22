/**
 * 封装所有GPRS网关客户端通道模块。
 * 由于上行报文需要进行处理，如告警自动应答、发送失败转短信通道、终端通道管理、流量统计、写报文日志等，
 * 需要把收到GPRS报文事件放入到优先级队列进行处理。
 * 封装所有短信客户端通道模块。
 * 
 * 设计要点：由于上行报文，都是有数据接收线程触发调用，因此在设计事件处理机制时，不需要例外启动事件处理线程。
 */
package com.hzjbbis.fk.bp.feclient;

import com.hzjbbis.fk.clientmod.ClientModule;

/**
 * @author bhw
 * 
 */
public class FEClientManage {
	//可配置属性，通过SPRING配置。
	ClientModule client;

	public void setClient(ClientModule feClient){
		client = feClient;
		FEChannelManage.addClient(feClient);
	}
}
