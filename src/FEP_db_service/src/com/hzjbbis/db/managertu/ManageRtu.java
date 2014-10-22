/**
 * 初始化加载RTU对象。
 * 通信前置机与业务处理器所需要加载的属性是不一样的。因此需要分别加载。
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
    	//初始化通讯前置机终端基本属性
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
		log.info(size+"个终端加载时间="+timeConsume+"ms;效率="+speed+"/s");
		
		startTime= System.currentTimeMillis();	
		log.info("start initializeTaskTemplate");
		size=initializeTaskTemplate();
		log.info("end initializeTaskTemplate");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		speed = size*1000 / timeConsume ;
		log.info(size+"个任务模版加载时间="+timeConsume+"ms;效率="+speed+"/s");
		
		startTime= System.currentTimeMillis();
		log.info("start initializeTaskDbConfig");
		size=initializeTaskDbConfig();
		log.info("end initializeTaskDbConfig");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		speed = size*1000 / timeConsume ;
		log.info(size+"个任务数据项数据库表映射关系加载时间="+timeConsume+"ms;效率="+speed+"/s");
		
		startTime= System.currentTimeMillis();
		log.info("start initializeAlertCode");
		size=initializeAlertCode();
		log.info("end initializeAlertCode");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		speed = size*1000 / timeConsume ;
		log.info(size+"个异常数据项加载时间="+timeConsume+"ms;效率="+speed+"/s");
		
		log.info("start initializeSysConfig");		
		size=initializeSysConfig();
		log.info("end initializeSysConfig");
	}
	
	/**
	 * 根据终端局号重新加载终端对象(包括终端资产,测量点信息及终端任务配置信息)。
	 * Use Case：主站主动通知前置机刷新终端信息。
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
	 * 根据终端RTUA重新加载终端对象(包括终端资产,测量点信息及终端任务配置信息)。
	 * Use Case: 任务解析失败，根据RTUA加载对象。
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
	 * 根据终端局号加载该终端的测量点信息列表。
	 * @param zdjh：终端局号
	 * @return
	 */
	public void refreshMeasurePoints(String zdjh){
		List<MeasuredPoint> mps=rtuRefreshDao.getMeasurePoints(zdjh);
		for( MeasuredPoint mp: mps ){
			rtuManage.putMeasuredPointToCache(mp);
		}
	}
	
	/**
	 * 刷新终端任务列表。
	 * @param zdjh： 终端局号。
	 * @return
	 */
	public void refreshRtuTasks(String zdjh){
		List<RtuTask> rts=rtuRefreshDao.getRtuTasks(zdjh);
		for( RtuTask rt: rts ){
			rtuManage.putRtuTaskToCache(rt);
		}
	}
	
	/**
	 * 刷新任务模板信息。
	 * Use Case：主站修改终端任务模板，在批量更新该模板的终端任务后，通知前置机刷新模板信息。
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
	 * 业务处理器终端档案(包括测量点和终端任务)初始化。
	 */
    private int initializeBizRtu(){ 
    	int size=0;
    	//初始化终端基本属性
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
    	//初始化终端测量点基本属性
    	try{			
			List<MeasuredPoint> mps = bizRtuDao.loadMeasuredPoints();
			log.info("MeasuredPointList size:"+mps.size());
			for( MeasuredPoint mp: mps ){
				rtuManage.putMeasuredPointToCache(mp);
			}
    	}catch(Exception ex){
    		log.error("loadMeasuredPoints"+ex);
    	} 
    	
    	//初始化终端任务配置基本属性
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
	 * 业务处理器任务模版初始化。
	 * @return
	 */
    private int initializeTaskTemplate(){
    	int size=0;
    	//初始化任务模版基本属性
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
	 * 初始化系统配置参数值
	 * @return
	 */
    public int initializeSysConfig(){
    	int size=0;
    	//初始化系统配置参数值
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
	 * 业务处理器任务保存映射信息初始化。
	 * @return
	 */
    public int initializeTaskDbConfig(){
    	int size=0;
    	//初始化任务保存映射信息基本属性
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
	 * 业务处理器异常携带数据项初始化。
	 * @return
	 */
    private int initializeAlertCode(){
    	int size=0;
    	//初始化异常携带数据项基本属性
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
