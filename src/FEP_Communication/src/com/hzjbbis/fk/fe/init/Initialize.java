/**
 * ����ն˶����ʼ�����̡�
 * ��������ݿ����ʧ�ܣ���ӱ����ļ����ء�
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
				log.warn("ͨ��ǰ�û�RTU���ݿ��ʼ��ʧ�ܣ�"+e.getLocalizedMessage(),e);
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
		
		//������ݿ����ʧ�ܣ���ӱ��ػ������
		RtuParamsCache.getInstance().initOnStartup( !loadDbSuccess );
		//Ϊ��֧�ֶ�ҵ��������ͳ��ÿ�������ն��������Ա����ַ�
		BpBalanceFactor.getInstance().travelRtus(RtuManage.getInstance().getAllComRtu());
		
		//����������Ϣ�Ķ�λ
		HeartbeatPersist.getInstance().initOnStartup();
		
		//���������������档
		RtuCommFlowCache.getInstance().initOnStartup();
		
		//ϵͳ�˳�ʱ����Ҫ�����ն�״̬�������ļ���
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
	 * Ϊ�˲���BP��Ⱥ�ķַ����ԣ���������ն˶���
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
