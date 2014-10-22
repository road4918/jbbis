/**
 * 管理通信参数与资产表不一致的RTU。
 */
package com.hzjbbis.fk.fe.filecache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.dao.jdbc.JdbcBatchDao;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;

/**
 * @author bhw
 *	注意事项：每次添加不一致情况，需要从数据库加载一次，记录加载时间。
 *  如果加载后还不一致，则把不一致记录到数据库。
 */
public class MisparamRtuManage {
	private static final Logger log = Logger.getLogger(MisparamRtuManage.class);
	//配置属性
	private JdbcBatchDao batchDao;
	private Object lock = new Object();
	private static MisparamRtuManage instance = null;
	
	private Map<Integer,MisparamRtu> disorders = new HashMap<Integer,MisparamRtu>();		//参数不一致

	private MisparamRtuManage(){}

	public static MisparamRtuManage getInstance(){
		if( null == instance )
			instance = new MisparamRtuManage();
		return instance;
	}
	
	public void addRtuByGprs(ComRtu rtu,String actGprsAddr ){
		synchronized(lock){
			MisparamRtu param = disorders.get(rtu.getRtua());
			if( null == param ){
				param = new MisparamRtu();
				param.setRtua(rtu.getRtua());
				disorders.put(param.getRtua(), param);
			}
			param.setGprsActiveCommAddr(actGprsAddr);
		}
	}
	
	public Collection<MisparamRtu> getAll(){
		return disorders.values();
	}
	
	public MisparamRtu get(int rtua){
		return disorders.get(rtua);
	}
	
	public void remove(int rtua){
		synchronized(disorders){
			disorders.remove(rtua);
		}
	}
	
	public void addRtuBySms(ComRtu rtu, String actSmsAddr ){
		synchronized(lock){
			MisparamRtu param = disorders.get(rtu.getRtua());
			if( null == param ){
				param = new MisparamRtu();
				param.setRtua(rtu.getRtua());
				disorders.put(param.getRtua(), param);
			}
			param.setSmsActiveCommAddr(actSmsAddr);
		}
	}
	
	/**
	 * 返回是否存在不一致需要保存。
	 * @return
	 */
	private boolean dirty(){
		return disorders.size() > 0 ;
	}
	
	/**
	 * 通过Spring配置定时任务执行保存。
	 */
	public void save(){
		if( !dirty() )
			return;
		//需要开始执行保存到数据库。
		synchronized(lock){
			//保存到数据库
			ArrayList<MisparamRtu> arr = new ArrayList<MisparamRtu>();
			for( MisparamRtu param: disorders.values() ){
				if( param.getLastUpdate() != 0 ){
					//已经更新过了，不要再次更新
					continue;
				}
				param.setLastUpdate();	//当前更新时间
				arr.add(param);
			}
			//更新arr包含的对象。
			
		}
	}
	
	/**
	 * 更新终端工况时，需要保存“不一致”信息。算法：
	 * 1）把所有不一致对象，按照rtua，设置到ComRtu对象中。
	 * 2）对每个ComRtu对象，如果找不到对应不一致，则当作一致，既相应属性=null
	 */
	public void merge2RtuCache(){
		synchronized(lock){
			for(ComRtu rtu: RtuManage.getInstance().getAllComRtu() ){
				MisparamRtu p = disorders.get(rtu.getRtua());
				String actGprs = null, actUms = null;
				if( null != p ){
					actGprs = p.getGprsActiveCommAddr();
					actUms = p.getSmsActiveCommAddr();
					p.setLastUpdate(0);			//允许下次提醒不一致。
				}
				//特别注意：RTU对象的activeGprs 存放的是网关内部IP:PORT，不是终端的主站通信参数
				rtu.setMisGprsAddress(actGprs);
				rtu.setMisSmsAddress(actUms);
			}
		}
	}

	public void merge2RtuCache(Collection<ComRtu> list){
		synchronized(lock){
			for(ComRtu rtu: list ){
				MisparamRtu p = disorders.get(rtu.getRtua());
				String actGprs = null, actUms = null;
				if( null != p ){
					actGprs = p.getGprsActiveCommAddr();
					actUms = p.getSmsActiveCommAddr();
					p.setLastUpdate(0);			//允许下次提醒不一致。
				}
				//特别注意：RTU对象的activeGprs 存放的是网关内部IP:PORT，不是终端的主站通信参数
				rtu.setMisGprsAddress(actGprs);
				rtu.setMisSmsAddress(actUms);
			}
		}
	}

	/**
	 * 直接把终端工况保存到数据库。不会自动清0.
	 */
	public void saveRtuStatus2Db(){
		log.info("saveRtuStatus2Db start...");
		synchronized(lock){
			merge2RtuCache();
			//调用DAO保存到数据库
			RtuManage rm = RtuManage.getInstance();
			List<ComRtu> list = null;
			synchronized(rm){
				list = new ArrayList<ComRtu>(rm.getAllComRtu());
			}
			for(ComRtu rtu: list )
				batchDao.add(rtu);
			batchDao.batchUpdate();
		}
		log.info("saveRtuStatus2Db end...");
	}
	
	public void saveRtuStatus2Db(Collection<ComRtu> list){
		log.info("saveRtuStatus2Db start...");
		synchronized(lock){
			merge2RtuCache(list);
			//调用DAO保存到数据库
			for(ComRtu rtu: list )
				batchDao.add(rtu);
			batchDao.batchUpdate();
		}
		log.info("saveRtuStatus2Db end...");
	}
	
	public void saveRtuStatus2DbPerDay(){
		log.info("saveRtuStatus2DbPerDay start...");
		RtuManage rm = RtuManage.getInstance();
		Collection<ComRtu> rtus = null;
		synchronized(rm){
			rtus = new ArrayList<ComRtu>(rm.getAllComRtu());
		}
		saveRtuStatus2Db(rtus);
		for(ComRtu rtu: rtus ){
			rtu.clearStatus();
		}
		RtuStatusCache.save2File(rtus);
		log.info("saveRtuStatus2DbPerDay end...");
	}

	public void setBatchDao(JdbcBatchDao batchDao) {
		this.batchDao = batchDao;
	}
	
}
