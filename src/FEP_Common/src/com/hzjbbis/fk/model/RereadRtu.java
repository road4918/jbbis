package com.hzjbbis.fk.model;


import java.util.HashMap;
import java.util.Map;


/**
 * ©�㲹���ն˵����ṹ
 */
public class RereadRtu {   
    /** �ն˾ֺ�ID */
    private String rtuId;        
    /** �ն������б�<���в���ID,����������Ϣ>*/
    private Map<Integer,RereadTask> rereadTasksMap=new HashMap<Integer,RereadTask>();

    public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public Map<Integer, RereadTask> getRereadTasksMap() {
		return rereadTasksMap;
	}
	public void setRereadTasksMap(Map<Integer, RereadTask> rereadTasksMap) {
		this.rereadTasksMap = rereadTasksMap;
	}

               	  		
}
