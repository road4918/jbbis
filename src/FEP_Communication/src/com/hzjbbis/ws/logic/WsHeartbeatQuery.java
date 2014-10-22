package com.hzjbbis.ws.logic;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface WsHeartbeatQuery {
	@WebMethod
	int heartCount( @WebParam(name = "rtua") int rtua);		//ĳ���ն˵Ľ�����������
	long lastHeartbeatTime( @WebParam(name = "rtua") int rtua);	//ĳ���ն����һ������ʱ��
	int totalRtuWithHeartByA1( @WebParam(name = "a1") byte a1 );	//ĳ�����е����������ն˵��ܺ�
	int totalRtuWithHeartByA1Time( @WebParam(name = "a1") byte a1, @WebParam(name = "beginTime") Date beginTime );	//��beginTime��ʼ���������ն��ܺ͡�
	String queryHeartbeatInfo( @WebParam(name = "rtua") int rtua);
	String queryHeartbeatInfoByDate( @WebParam(name = "rtua") int rtua, @WebParam(name = "date") int date);
}
