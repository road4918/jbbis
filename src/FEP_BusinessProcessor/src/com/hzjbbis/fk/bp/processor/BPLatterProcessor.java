package com.hzjbbis.fk.bp.processor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.db.bizprocess.MasterDbService;
import com.hzjbbis.fk.bp.model.AlarmData;
import com.hzjbbis.fk.bp.model.TaskItemData;
import com.hzjbbis.util.Counter;

/**
 * ҵ�������
 * 
 */
public class BPLatterProcessor {
	private static final Log log = LogFactory.getLog(BPLatterProcessor.class);
    /** ���� */
    private static BPLatterProcessor instance;
    /** �����߳� */
    private Worker worker;
    /** �洢���̷���ģ�� */
    private MasterDbService masterDbService;
    /** �������ֵ */
    private static final int LIST_MAX_COUNT = 1000000;
    /** �����������ʱ�� */
    private static final int UPDATE_MAX_TIME = 1000;
    /** �����̵߳������ʱ�䣨ms�� */
    private static final int WORKER_MAX_STEP_TIME = 5000;
    /** �������ݶ��� */
    private static List<TaskItemData> taskDataList=Collections.synchronizedList(new LinkedList<TaskItemData>());
	/** �쳣���ݶ��� */
	private static List<AlarmData> alertDataList=Collections.synchronizedList(new LinkedList<AlarmData>());
	private static final Object listSync = new Object();
	
	private Counter taskCounter=new Counter(1000,"TaskLatterP");
	private Counter AlertCounter=new Counter(1000,"AlarmLatterP");
	public BPLatterProcessor() {				   	
	}
	public boolean start() {
		if (masterDbService!=null){
			this.worker=new Worker();
			this.worker.start();
			if( log.isInfoEnabled())
				log.info("ҵ�����������߳������ɹ�");
			return true;
		}
		else{
			if( log.isInfoEnabled() )
				log.info("ҵ�����������߳�����ʧ��:�洢���̷���ģ��û������");
			return false;
		}
	}
	/**
     * ȡ���ն˻��������������δ��ʼ�������ڳ�ʼ���󷵻�
     * @return �ն˻������
     */
    public static BPLatterProcessor getInstance() {
        if (instance == null) {
            synchronized (BPLatterProcessor.class) {
                if (instance == null) {
                    instance = new BPLatterProcessor();
                }
            }
        }       
        return instance;
    }
	public void rtuDataAdd(TaskItemData data) {
		synchronized( listSync ){
			taskDataList.add(data);
			listSync.notifyAll();
		}
	}
	public void alertDataAdd(AlarmData alarm) {
		synchronized( listSync ){
			alertDataList.add(alarm);
			listSync.notifyAll();
		}
	}
	/**
     * ���ӹ����̡߳�����й����̵߳ȴ�ʱ�䳬����ֵ,���ӡ��Ϣ
     */
    public void monitorWorkerThreads() {
    	long now = System.currentTimeMillis();   	
	    if (now - worker.getLastSaveTime() >= WORKER_MAX_STEP_TIME) {	            
	        log.warn(
        		"DBBP Worker timeOut:"+
        		"taskDataList.size="+taskDataList.size()+
        		"alertDataList.size="+alertDataList.size()+
        		worker.toString());	        
	    } 	   
    }
    private class Worker extends Thread {
    	/** �߳���ֹ��־ */
    	private boolean shouldTerminate=false;
    	private long saveTime=System.currentTimeMillis();
    	private long curTime=System.currentTimeMillis();
    	private long lastSaveTime=System.currentTimeMillis();
    	public Worker() {
        }  
    	public long getLastSaveTime() {
            return lastSaveTime;
        }
    	
    	/*
    	 * (non-Javadoc)
    	 * 
    	 * @see java.lang.Thread#run()
    	 */
    	public void run() {
    		int alertCount=0,taskCount=0;    		
			while (true) {
				if (shouldTerminate)
					break;
				//�ȴ�����
				synchronized( listSync ){
					try{
						if (taskDataList.size()<=0&&alertDataList.size()<=0)
							listSync.wait();
					}catch(Exception e){}
				}
				if (taskDataList.size()>0){
					TaskItemData data=(TaskItemData)taskDataList.get(0);
					taskDataList.remove(0);
					if (taskDataList.size()<LIST_MAX_COUNT){//������ֵ����
						curTime= System.currentTimeMillis();		
						try{
							masterDbService.procPostCreateRtuData(data);
							lastSaveTime=System.currentTimeMillis();
							saveTime=lastSaveTime-curTime;
							if (saveTime>UPDATE_MAX_TIME)
								log.info("postCreateRtuData="+saveTime);
	    				}catch(Exception ex){
	                    	log.error("postCreateRtuData error", ex);
	                    }					
	    				taskCount=taskDataList.size();
						if (taskCount>=1000&&taskCount % 1000==0){
							log.warn("taskDataList size="+taskCount);
						}
						taskCounter.add();													
					}												
				}					
				if (alertDataList.size()>0){//�쳣����
					AlarmData alert=(AlarmData)alertDataList.get(0);
					alertDataList.remove(0);
					if (alertDataList.size()<LIST_MAX_COUNT){//������ֵ����
						curTime= System.currentTimeMillis();
						if(alert.getTxfs()==null)//ͨѶ��ʽδ֪
							alert.setTxfs("99");
						try{
							masterDbService.procPostCreateRtuAlert(alert);
							lastSaveTime=System.currentTimeMillis();
							saveTime=lastSaveTime-curTime;
							if (saveTime>UPDATE_MAX_TIME)
								log.info("postCreateRtuAlert="+saveTime);
						}catch(Exception ex){
	                    	log.error("postCreateRtuAlert error", ex);
	                    }					
						alertCount=alertDataList.size();
						if (alertCount>=1000&&alertCount % 1000==0){
							log.warn("alertDataList size="+alertCount);
						}
						AlertCounter.add();							
					}					
				}
			}
    	}    			
	}
	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
	

}
