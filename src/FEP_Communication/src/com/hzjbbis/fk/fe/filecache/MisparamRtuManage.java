/**
 * ����ͨ�Ų������ʲ���һ�µ�RTU��
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
 *	ע�����ÿ����Ӳ�һ���������Ҫ�����ݿ����һ�Σ���¼����ʱ�䡣
 *  ������غ󻹲�һ�£���Ѳ�һ�¼�¼�����ݿ⡣
 */
public class MisparamRtuManage {
	private static final Logger log = Logger.getLogger(MisparamRtuManage.class);
	//��������
	private JdbcBatchDao batchDao;
	private Object lock = new Object();
	private static MisparamRtuManage instance = null;
	
	private Map<Integer,MisparamRtu> disorders = new HashMap<Integer,MisparamRtu>();		//������һ��

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
	 * �����Ƿ���ڲ�һ����Ҫ���档
	 * @return
	 */
	private boolean dirty(){
		return disorders.size() > 0 ;
	}
	
	/**
	 * ͨ��Spring���ö�ʱ����ִ�б��档
	 */
	public void save(){
		if( !dirty() )
			return;
		//��Ҫ��ʼִ�б��浽���ݿ⡣
		synchronized(lock){
			//���浽���ݿ�
			ArrayList<MisparamRtu> arr = new ArrayList<MisparamRtu>();
			for( MisparamRtu param: disorders.values() ){
				if( param.getLastUpdate() != 0 ){
					//�Ѿ����¹��ˣ���Ҫ�ٴθ���
					continue;
				}
				param.setLastUpdate();	//��ǰ����ʱ��
				arr.add(param);
			}
			//����arr�����Ķ���
			
		}
	}
	
	/**
	 * �����ն˹���ʱ����Ҫ���桰��һ�¡���Ϣ���㷨��
	 * 1�������в�һ�¶��󣬰���rtua�����õ�ComRtu�����С�
	 * 2����ÿ��ComRtu��������Ҳ�����Ӧ��һ�£�����һ�£�����Ӧ����=null
	 */
	public void merge2RtuCache(){
		synchronized(lock){
			for(ComRtu rtu: RtuManage.getInstance().getAllComRtu() ){
				MisparamRtu p = disorders.get(rtu.getRtua());
				String actGprs = null, actUms = null;
				if( null != p ){
					actGprs = p.getGprsActiveCommAddr();
					actUms = p.getSmsActiveCommAddr();
					p.setLastUpdate(0);			//�����´����Ѳ�һ�¡�
				}
				//�ر�ע�⣺RTU�����activeGprs ��ŵ��������ڲ�IP:PORT�������ն˵���վͨ�Ų���
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
					p.setLastUpdate(0);			//�����´����Ѳ�һ�¡�
				}
				//�ر�ע�⣺RTU�����activeGprs ��ŵ��������ڲ�IP:PORT�������ն˵���վͨ�Ų���
				rtu.setMisGprsAddress(actGprs);
				rtu.setMisSmsAddress(actUms);
			}
		}
	}

	/**
	 * ֱ�Ӱ��ն˹������浽���ݿ⡣�����Զ���0.
	 */
	public void saveRtuStatus2Db(){
		log.info("saveRtuStatus2Db start...");
		synchronized(lock){
			merge2RtuCache();
			//����DAO���浽���ݿ�
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
			//����DAO���浽���ݿ�
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
