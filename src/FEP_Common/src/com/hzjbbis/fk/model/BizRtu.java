package com.hzjbbis.fk.model;


import java.util.HashMap;
import java.util.Map;


/**
 * 业务处理器终端档案结构
 */
public class BizRtu {   
    /** 终端局号ID */
    private String rtuId;
    /** 单位代码 */
    private String deptCode;
    /** 终端规约类型 */
    private String rtuProtocol;
    /** 终端逻辑地址 */
    private int rtua;
    /** 终端逻辑地址（HEX） */
    private String logicAddress;
    /** 厂商（编号） */
    private String manufacturer;
    /** 高权限密码 */
    private String hiAuthPassword;
    /** 低权限密码 */
    private String loAuthPassword;      
    /** 测量点列表 */
    private Map<String,MeasuredPoint> measuredPoints=new HashMap<String,MeasuredPoint>();
    /** 终端任务列表 */
    private Map<Integer,RtuTask> tasksMap=new HashMap<Integer,RtuTask>();

    
    
    
    /**
     * 根据测量点号取得测量点
     * @param tn 测量点号
     * @return 测量点。如果不存在，则返回 null
     */
    public MeasuredPoint getMeasuredPoint(String tn) {
    	return (MeasuredPoint) measuredPoints.get(tn);
    }
    
    /**
     * 添加测量点
     * @param mp 测量点
     */
    public void addMeasuredPoint(MeasuredPoint mp) {
        measuredPoints.put(mp.getTn(),mp);                
    }
    /**
     * 添加测量点
     * @param mp 测量点
     */
    public void addRtuTask(RtuTask rt) {
    	tasksMap.put(new Integer(rt.getRtuTaskNum()), rt);            
    }     
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[id=").append(rtuId)
            .append(", logicAddress=").append(logicAddress)
            .append(", protocol=").append(rtuProtocol)
            .append(", manufacturer=").append(manufacturer).append(", ... ]");
        return sb.toString();
    }
    public RtuTask getRtuTask(String taskNum) {   
    	if (tasksMap == null || taskNum == null) {
            return null;
        }
    	return (RtuTask)tasksMap.get(new Integer(taskNum)); 
    }
    /**
     * 根据任务号取得终端任务及保存所需信息
     * @param taskNum 任务号
     * @return 终端任务。如果没有对应的任务，则返回 null
     */
    public TaskTemplate getTaskTemplate(String taskNum) {
        if (tasksMap == null || taskNum == null) {
            return null;
        }
        RtuTask rt=(RtuTask)tasksMap.get(new Integer(taskNum)); 
        if(rt !=null){
        	return RtuManage.getInstance().getTaskPlateInCache(rt.getTaskTemplateID());
        	/*MeasuredPoint mp=getMeasuredPoint(rt.getTn());
        	if (mp!=null){
        		tp.setDeptCode(deptCode);        		
        		tp.setTn(mp.getTn());
            	tp.setDataSaveID(mp.getDataSaveID());
            	tp.setCt(mp.getCt());
            	tp.setPt(mp.getPt());
        	}  */      
        	//return tp;
        }                
        return null;
    }
    
    
    /**
     * @return Returns the id.
     */
    public String getRtuId() {
        return rtuId;
    }
    /**
     * @param id The id to set.
     */
    public void setRtuId(String rtuId) {
        this.rtuId = rtuId;
    }
      
    /**
	 * @return 返回 rtuProtocol。
	 */
	public String getRtuProtocol() {
		return rtuProtocol;
	}

	/**
	 * @param rtuProtocol 要设置的 rtuProtocol。
	 */
	public void setRtuProtocol(String rtuProtocol) {
		this.rtuProtocol = rtuProtocol;
	}

	/**
     * @return Returns the rtua.
     */
    public int getRtua() {
        return rtua;
    }
    /**
     * @param rtua The rtua to set.
     */
    public void setRtua(int rtua) {
        this.rtua = rtua;
    }
    /**
     * @return Returns the logicAddress.
     */
    public String getLogicAddress() {
        return logicAddress;
    }
    /**
     * @param logicAddress The logicAddress to set.
     */
    public void setLogicAddress(String logicAddress) {
        this.logicAddress = logicAddress;
    }
    
    
    /**
     * @return Returns the manufacturer.
     */
    public String getManufacturer() {
        return manufacturer;
    }
    /**
     * @param manufacturer The manufacturer to set.
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    /**
     * @return Returns the hiAuthPassword.
     */
    public String getHiAuthPassword() {
        return hiAuthPassword;
    }
    /**
     * @param hiAuthPassword The hiAuthPassword to set.
     */
    public void setHiAuthPassword(String hiAuthPassword) {
        this.hiAuthPassword = hiAuthPassword;
    }
    /**
     * @return Returns the loAuthPassword.
     */
    public String getLoAuthPassword() {
        return loAuthPassword;
    }
    /**
     * @param loAuthPassword The loAuthPassword to set.
     */
    public void setLoAuthPassword(String loAuthPassword) {
        this.loAuthPassword = loAuthPassword;
    }
        

	public String getDeptCode() {
		return deptCode;
	}
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
    
	
  		
}
