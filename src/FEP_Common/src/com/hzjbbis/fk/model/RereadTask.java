package com.hzjbbis.fk.model;

import java.util.HashMap;
import java.util.Map;


/**
 * ©�㲹��������Ϣ
 */
public class RereadTask {   
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ����� */
    private String taskNum;
    /** ������ */
    private String taskInterval;
    /** ���в���ID */
    private Integer rereadPolicyID;  
    /** ����ʱ����б�<����ʱ�䣬������Ϣ��> */
    private Map<Long,RereadInfo> datemaps=new HashMap<Long,RereadInfo>();
    
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public String getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(String taskNum) {
		this.taskNum = taskNum;
	}
	public String getTaskInterval() {
		return taskInterval;
	}
	public void setTaskInterval(String taskInterval) {
		this.taskInterval = taskInterval;
	}
	public Integer getRereadPolicyID() {
		return rereadPolicyID;
	}
	public void setRereadPolicyID(Integer rereadPolicyID) {
		this.rereadPolicyID = rereadPolicyID;
	}
	public Map<Long, RereadInfo> getDatemaps() {
		return datemaps;
	}
	public void setDatemaps(Map<Long, RereadInfo> datemaps) {
		this.datemaps = datemaps;
	}
	
}
