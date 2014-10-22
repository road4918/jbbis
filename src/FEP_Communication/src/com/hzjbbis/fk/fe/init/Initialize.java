/**
 * 完成终端对象初始化过程。
 * 如果从数据库加载失败，则从本地文件加载。
 */
package com.hzjbbis.fk.fe.init;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.db.DbMonitor;
import com.hzjbbis.db.initrtu.dao.ComRtuDao;
import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.fe.filecache.HeartbeatPersist;
import com.hzjbbis.fk.fe.filecache.RtuCommFlowCache;
import com.hzjbbis.fk.fe.filecache.RtuParamsCache;
import com.hzjbbis.fk.fe.msgqueue.BpBalanceFactor;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.tracelog.TraceLog;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 *
 */
public class Initialize {
	private static final Logger log = Logger.getLogger(Initialize.class);
	private ComRtuDao rtuDao;
	private boolean bpClusterTest = false;

	public void setRtuDao(ComRtuDao rtuDao) {
		this.rtuDao = rtuDao;
	}

	public void initRtus(){
		TraceLog.getTracer().trace("initRtus called");
		List<ComRtu> rtus = null;
		boolean loadDbSuccess = false;
		if( ! bpClusterTest ){
			try{
				if( DbMonitor.getMasterMonitor().isAvailable() ){
					rtus = rtuDao.loadComRtu();
					loadDbSuccess = true;
				}
			}catch(Exception e){
				log.warn("通信前置机RTU数据库初始化失败："+e.getLocalizedMessage(),e);
			}
			if( null != rtus ){
				for( ComRtu rtu: rtus )
					RtuManage.getInstance().putComRtuToCache(rtu);
			}
		}
		else{
			loadDbSuccess = true;
			rtus = testCase4BpCluster();
			for( ComRtu rtu: rtus )
				RtuManage.getInstance().putComRtuToCache(rtu);
		}
		
		//如果数据库加载失败，则从本地缓存加载
		RtuParamsCache.getInstance().initOnStartup( !loadDbSuccess );
		//为了支持多业务处理器，统计每个地市终端数量，以便均衡分发
		BpBalanceFactor.getInstance().travelRtus(RtuManage.getInstance().getAllComRtu());
		
		//心跳缓存信息的定位
		HeartbeatPersist.getInstance().initOnStartup();
		
		//继续加载流量缓存。
		RtuCommFlowCache.getInstance().initOnStartup();
		
		//系统退出时候，需要保存终端状态到本地文件。
		FasSystem.getFasSystem().addShutdownHook(new Runnable(){

			public void run() {
				shutdownWork();
			}
			
		});
	}

	private void shutdownWork(){
		RtuParamsCache.getInstance().dispose();
		RtuCommFlowCache.getInstance().dispose();
		HeartbeatPersist.getInstance().dispose();
	}
	
	/**
	 * 为了测试BP集群的分发策略，创建多个终端对象。
	 */
	private List<ComRtu> testCase4BpCluster(){
		List<ComRtu> list = new LinkedList<ComRtu>();
		Map<Integer,Integer> rtuMap = new HashMap<Integer,Integer>();
		rtuMap.put(0x91010001, 2);
		rtuMap.put(0x92010001, 3);
		rtuMap.put(0x93010001, 5);
		rtuMap.put(0x94010001, 6);
		rtuMap.put(0x95010001, 7);
		rtuMap.put(0x96010001, 8);
		rtuMap.put(0x97010001, 11);
		rtuMap.put(0x98010001, 12);
		rtuMap.put(0x99010001, 13);
		Iterator<Map.Entry<Integer,Integer>> iter = rtuMap.entrySet().iterator();
		while( iter.hasNext() ){
			Map.Entry<Integer,Integer> entry = iter.next();
			for(int i=0; i<entry.getValue(); i++){
				ComRtu rtu = new ComRtu();
				int rtua = entry.getKey() + i;
				rtu.setRtua(rtua);
				rtu.setLogicAddress(HexDump.toHex(rtua));
				list.add(rtu);
			}
		}
		return list;
	}

	public final void setBpClusterTest(boolean bpClusterTest) {
		this.bpClusterTest = bpClusterTest;
	}
}
