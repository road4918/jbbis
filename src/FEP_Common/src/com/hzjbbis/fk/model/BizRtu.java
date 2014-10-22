package com.hzjbbis.fk.model;


import java.util.HashMap;
import java.util.Map;


/**
 * ҵ�������ն˵����ṹ
 */
public class BizRtu {   
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ��λ���� */
    private String deptCode;
    /** �ն˹�Լ���� */
    private String rtuProtocol;
    /** �ն��߼���ַ */
    private int rtua;
    /** �ն��߼���ַ��HEX�� */
    private String logicAddress;
    /** ���̣���ţ� */
    private String manufacturer;
    /** ��Ȩ������ */
    private String hiAuthPassword;
    /** ��Ȩ������ */
    private String loAuthPassword;      
    /** �������б� */
    private Map<String,MeasuredPoint> measuredPoints=new HashMap<String,MeasuredPoint>();
    /** �ն������б� */
    private Map<Integer,RtuTask> tasksMap=new HashMap<Integer,RtuTask>();

    
    
    
    /**
     * ���ݲ������ȡ�ò�����
     * @param tn �������
     * @return �����㡣��������ڣ��򷵻� null
     */
    public MeasuredPoint getMeasuredPoint(String tn) {
    	return (MeasuredPoint) measuredPoints.get(tn);
    }
    
    /**
     * ��Ӳ�����
     * @param mp ������
     */
    public void addMeasuredPoint(MeasuredPoint mp) {
        measuredPoints.put(mp.getTn(),mp);                
    }
    /**
     * ��Ӳ�����
     * @param mp ������
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
     * ���������ȡ���ն����񼰱���������Ϣ
     * @param taskNum �����
     * @return �ն��������û�ж�Ӧ�������򷵻� null
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
	 * @return ���� rtuProtocol��
	 */
	public String getRtuProtocol() {
		return rtuProtocol;
	}

	/**
	 * @param rtuProtocol Ҫ���õ� rtuProtocol��
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
