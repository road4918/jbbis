package com.hzjbbis.ws.logic;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface WsFEManage {
	boolean addGprsGateChannel(@WebParam(name = "ip") String ip,@WebParam(name = "port") int port,@WebParam(name = "gateName") String gateName);
	boolean addUmsChannel(@WebParam(name = "appid") String appid,@WebParam(name = "password") String password);
	void stopModule(@WebParam(name = "name") String name);
	void startModule(@WebParam(name = "name") String name);
	void updateFlow();		//更新流量统计。
}
