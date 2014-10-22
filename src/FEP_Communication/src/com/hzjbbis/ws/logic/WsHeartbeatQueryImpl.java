package com.hzjbbis.ws.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jws.WebService;

import com.hzjbbis.fk.fe.filecache.HeartbeatPersist;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;

@WebService(endpointInterface = "com.hzjbbis.ws.logic.WsHeartbeatQuery")
public class WsHeartbeatQueryImpl implements WsHeartbeatQuery {
	
	//某个终端的今天心跳次数
	public int heartCount(int rtua){
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		return null == rtu? -1 : rtu.getHeartbeatCount();
	}
	
	//某个终端最后一次心跳时间
	public long lastHeartbeatTime(int rtua){
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		return null == rtu ? 0 : rtu.getLastHeartbeat();
	}

	//某个地市的有心跳的终端的总和
	public int totalRtuWithHeartByA1( byte a1 ){
		List<ComRtu> list = new ArrayList<ComRtu>(RtuManage.getInstance().getAllComRtu());
		int sum = 0;
		for(ComRtu rtu: list){
			int rtua = rtu.getRtua() & 0xFF000000 ;
			int ia1 = (a1 << 24) & 0xFF000000 ;
			if( rtua == ia1 && rtu.getHeartbeatCount()>0 )
				sum++;
		}
		return sum;
	}

	//从beginTime开始有心跳的终端总和。
	public int totalRtuWithHeartByA1Time( byte a1, Date beginTime ){
		List<ComRtu> list = new ArrayList<ComRtu>(RtuManage.getInstance().getAllComRtu());
		int sum = 0;
		for(ComRtu rtu: list){
			int rtua = rtu.getRtua() & 0xFF000000 ;
			int ia1 = (a1 << 24) & 0xFF000000 ;
			if( rtua == ia1 && rtu.getHeartbeatCount()>0 && beginTime.before(rtu.getLastHeartbeatTime()) )
				sum++;
		}
		return sum;
	}
	
	public String queryHeartbeatInfo( int rtua){
		return HeartbeatPersist.getInstance().queryHeartbeatInfo(rtua);
	}

	public String queryHeartbeatInfoByDate( int rtua, int date){
		return HeartbeatPersist.getInstance().queryHeartbeatInfo(rtua,date);
	}
}
