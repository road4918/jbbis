package com.hzjbbis.ws.logic;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface WsHeartbeatQuery {
	@WebMethod
	int heartCount( @WebParam(name = "rtua") int rtua);		//某个终端的今天心跳次数
	long lastHeartbeatTime( @WebParam(name = "rtua") int rtua);	//某个终端最后一次心跳时间
	int totalRtuWithHeartByA1( @WebParam(name = "a1") byte a1 );	//某个地市的有心跳的终端的总和
	int totalRtuWithHeartByA1Time( @WebParam(name = "a1") byte a1, @WebParam(name = "beginTime") Date beginTime );	//从beginTime开始有心跳的终端总和。
	String queryHeartbeatInfo( @WebParam(name = "rtua") int rtua);
	String queryHeartbeatInfoByDate( @WebParam(name = "rtua") int rtua, @WebParam(name = "date") int date);
}
