/**
 * ��ʼ������RTU����
 * ͨ��ǰ�û���ҵ����������Ҫ���ص������ǲ�һ���ġ������Ҫ�ֱ���ء�
 */
package com.hzjbbis.db.managertu;

import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.BatchDaoParameterUtils;
import com.hzjbbis.db.initrtu.dao.BizRtuDao;
import com.hzjbbis.db.initrtu.dao.ComRtuDao;
import com.hzjbbis.db.rtu.RtuRefreshDao;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuAlertCode;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.SysConfig;
import com.hzjbbis.fk.model.TaskDbConfig;
import com.hzjbbis.fk.model.TaskTemplate;
import com.hzjbbis.fk.model.TaskTemplateItem;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 *
 */
public class ManageRtu {
	private static final Logger log = Logger.getLogger(ManageRtu.class);
	private ComRtuDao comRtuDao;
	private BizRtuDao bizRtuDao;
	private RtuRefreshDao rtuRefreshDao; 
	public RtuManage rtuManage=RtuManage.getInstance();

	public void loadComRtu(){
		log.info("start initializeComRtu");
    	//��ʼ��ͨѶǰ�û��ն˻�������
    	try{
    		List<ComRtu> comRtus=comRtuDao.loadComRtu();		
			log.info("ComRtus size:"+comRtus.size());
			for( ComRtu rtu: comRtus ){
				rtuManage.putComRtuToCache(rtu);
			}
    	}catch(Exception ex){
    		log.error("loadComRtus"+ex);
    	} 
		log.info("end initializeComRtu");		
	}
	
	public void loadBizRtu(){	
		int size=0;
		long startTime= System.currentTimeMillis();	
		log.info("start initializeBizRtu");
		size=initializeBizRtu();
		log.info("end initializeBizRtu");
		long endTime = System.currentTimeMillis();
		long timeConsume = endTime-startTime;
		long speed = size*1000 / timeConsume ;
		log.info(size+"���ն˼���ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		
		startTime= System.currentTimeMillis();	
		log.info("start initializeTaskTemplate");
		size=initializeTaskTemplate();
		log.info("end initializeTaskTemplate");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		speed = size*1000 / timeConsume ;
		log.info(size+"������ģ�����ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		
		startTime= System.currentTimeMillis();
		log.info("start initializeTaskDbConfig");
		size=initializeTaskDbConfig();
		log.info("end initializeTaskDbConfig");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		speed = size*1000 / timeConsume ;
		log.info(size+"���������������ݿ��ӳ���ϵ����ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		
		startTime= System.currentTimeMillis();
		log.info("start initializeAlertCode");
		size=initializeAlertCode();
		log.info("end initializeAlertCode");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		speed = size*1000 / timeConsume ;
		log.info(size+"���쳣���������ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		
		log.info("start initializeSysConfig");		
		size=initializeSysConfig();
		log.info("end initializeSysConfig");
	}
	
	/**
	 * �����ն˾ֺ����¼����ն˶���(�����ն��ʲ�,��������Ϣ���ն�����������Ϣ)��
	 * Use Case����վ����֪ͨǰ�û�ˢ���ն���Ϣ��
	 * @param zdjh
	 * @return
	 */
	public boolean refreshBizRtu(String zdjh){
		try{
			BizRtu bizRtu=rtuRefreshDao.getRtu(zdjh);
			if (bizRtu!=null){
				rtuManage.putBizRtuToCache(bizRtu);	
				return true;
			}
			else return false;
		}catch(Exception ex){
			log.error("find not rtuId:"+zdjh);
			return false;
		}
	}
	
	/**
	 * �����ն�RTUA���¼����ն˶���(�����ն��ʲ�,��������Ϣ���ն�����������Ϣ)��
	 * Use Case: �������ʧ�ܣ�����RTUA���ض���
	 * @param rtua
	 * @return
	 */
	public boolean refreshBizRtu(int rtua){
		try{
			BizRtu bizRtu=rtuRefreshDao.getRtu(rtua);
			if (bizRtu!=null){
				rtuManage.putBizRtuToCache(bizRtu);
				return true;
			}
			else
				return false;
		}catch(Exception ex){
			log.error("find not rtuAdd:"+HexDump.toHex(rtua));
			return false;
		}
	}
	
	/**
	 * �����ն˾ֺż��ظ��ն˵Ĳ�������Ϣ�б�
	 * @param zdjh���ն˾ֺ�
	 * @return
	 */
	public void refreshMeasurePoints(String zdjh){
		List<MeasuredPoint> mps=rtuRefreshDao.getMeasurePoints(zdjh);
		for( MeasuredPoint mp: mps ){
			rtuManage.putMeasuredPointToCache(mp);
		}
	}
	
	/**
	 * ˢ���ն������б�
	 * @param zdjh�� �ն˾ֺš�
	 * @return
	 */
	public void refreshRtuTasks(String zdjh){
		List<RtuTask> rts=rtuRefreshDao.getRtuTasks(zdjh);
		for( RtuTask rt: rts ){
			rtuManage.putRtuTaskToCache(rt);
		}
	}
	
	/**
	 * ˢ������ģ����Ϣ��
	 * Use Case����վ�޸��ն�����ģ�壬���������¸�ģ����ն������֪ͨǰ�û�ˢ��ģ����Ϣ��
	 * @param templID
	 * @return
	 */
	public void refreshTaskTemplate(String templID){
		TaskTemplate tt = rtuRefreshDao.getTaskTemplate(templID);
		rtuManage.putTaskTemplateToCache(tt);
		List<TaskTemplateItem> ttis = rtuRefreshDao.getTaskTemplateItems(templID);
		for( TaskTemplateItem tti: ttis ){
			rtuManage.putTaskTemplateItemToCache(tti);
		}
	}
		
	/**
	 * ҵ�������ն˵���(������������ն�����)��ʼ����
	 */
    private int initializeBizRtu(){ 
    	int size=0;
    	//��ʼ���ն˻�������
    	try{
			List<BizRtu> bizRtus = bizRtuDao.loadBizRtu();
			size=bizRtus.size();
			log.info("BizRtuList size:"+bizRtus.size());
			for( BizRtu rtu: bizRtus ){
				rtuManage.putBizRtuToCache(rtu);
			}
    	}catch(Exception ex){
    		log.error("loadBizRtus"+ex);
    		return size;
    	} 
    	//��ʼ���ն˲������������
    	try{			
			List<MeasuredPoint> mps = bizRtuDao.loadMeasuredPoints();
			log.info("MeasuredPointList size:"+mps.size());
			for( MeasuredPoint mp: mps ){
				rtuManage.putMeasuredPointToCache(mp);
			}
    	}catch(Exception ex){
    		log.error("loadMeasuredPoints"+ex);
    	} 
    	
    	//��ʼ���ն��������û�������
    	try{
			
			List<RtuTask> rts = bizRtuDao.loadRtuTasks();
			log.info("RtuTaskList size:"+rts.size());
			for( RtuTask rt: rts ){
				rtuManage.putRtuTaskToCache(rt);
			}
    	}catch(Exception ex){
    		log.error("loadRtuTasks"+ex);
    	} 
    	return size;
    }
    /**
	 * ҵ����������ģ���ʼ����
	 * @return
	 */
    private int initializeTaskTemplate(){
    	int size=0;
    	//��ʼ������ģ���������
    	try{
			List<TaskTemplate> tts = bizRtuDao.loadTaskTemplate();
			size=tts.size();
			log.info("TaskTemplateList size:"+tts.size());
			for( TaskTemplate tt: tts ){
				rtuManage.putTaskTemplateToCache(tt);
			}
    	}catch(Exception ex){
    		log.error("loadTaskTemplate"+ex);
    		return size;
    	} 
    	return size;
    }
    /**
	 * ��ʼ��ϵͳ���ò���ֵ
	 * @return
	 */
    public int initializeSysConfig(){
    	int size=0;
    	//��ʼ��ϵͳ���ò���ֵ
    	try{
			List<SysConfig> sc = bizRtuDao.loadSysConfig();
			size=sc.size();
			log.info("SysConfig size:"+sc.size());
			if (size==2){
				SysConfig sysConfig=new SysConfig();
				sysConfig.setBj10(((SysConfig)sc.get(0)).getPzz());
				sysConfig.setBj11(((SysConfig)sc.get(1)).getPzz());
				rtuManage.setSysConfig(sysConfig);
				BatchDaoParameterUtils.getInstance().setAdditiveParameter(RtuManage.getInstance().getSysConfig());
			}			
    	}catch(Exception ex){
    		log.error("loadSysConfig"+ex);
    		return size;
    	} 
    	return size;
    }
    /**
	 * ҵ���������񱣴�ӳ����Ϣ��ʼ����
	 * @return
	 */
    public int initializeTaskDbConfig(){
    	int size=0;
    	//��ʼ�����񱣴�ӳ����Ϣ��������
    	try{
			List<TaskDbConfig> rdcs = bizRtuDao.loadTaskDbConfig();
			size=rdcs.size();
			log.info("TaskDbConfigList size:"+rdcs.size());
			for( TaskDbConfig rdc: rdcs ){
				rtuManage.putTaskDbConfigToCache(rdc);
			}
    	}catch(Exception ex){
    		log.error("loadTaskDbConfig"+ex);
    	} 
    	return size;
    }
    /**
	 * ҵ�������쳣Я���������ʼ����
	 * @return
	 */
    private int initializeAlertCode(){
    	int size=0;
    	//��ʼ���쳣Я���������������
    	try{
			List<RtuAlertCode> racs = bizRtuDao.loadRtuAlertCodes();
			size=racs.size();
			log.info("RtuAlertCodeList size:"+racs.size());
			for( RtuAlertCode rac: racs ){
				rtuManage.putAlertCodeToCache(rac);
			}
    	}catch(Exception ex){
    		log.error("loadRtuAlertCodes"+ex);
    	} 
    	return size;
    }
    public void setComRtuDao(ComRtuDao comRtuDao) {
		this.comRtuDao = comRtuDao;
	}
	public void setBizRtuDao(BizRtuDao bizRtuDao) {
		this.bizRtuDao = bizRtuDao;
	}
	public void setRtuRefreshDao(RtuRefreshDao rtuRefreshDao) {
		this.rtuRefreshDao = rtuRefreshDao;
	}
}
