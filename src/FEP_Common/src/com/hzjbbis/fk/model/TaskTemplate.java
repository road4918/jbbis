package com.hzjbbis.fk.model;

import java.util.ArrayList;
import java.util.List;

import com.hzjbbis.fk.utils.HexDump;

/**
 * 终端任务
 * @author 张文亮
 */
public class TaskTemplate {

    /** 任务类型: 普通任务 */
    public static final String TASK_TYPE_NORMAL = "01";
    /** 任务类型: 中继任务 */
    public static final String TASK_TYPE_FORWARD = "02";   
    /** 任务配置字符串中各属性的分隔符 */
    private static final String DELIM = ",";
    
    /** 任务模版ID */
    private String taskTemplateID;
    /** 任务类型 */
    private String taskType;	
    /** 采样开始基准时间 */
    private int sampleStartTime;
    /** 采样开始基准时间单位 */
    private String sampleStartTimeUnit;
    /** 采样间隔时间 */
    private int sampleInterval;
    /** 采样间隔时间单位 */
    private String sampleIntervalUnit;
    /** 上送基准时间 */
    private int uploadStartTime;
    /** 上送基准时间单位 */
    private String uploadStartTimeUnit;
    /** 上送间隔时间 */
    private int uploadInterval;
    /** 上送间隔时间单位 */
    private String uploadIntervalUnit;
    /** 上报数据频率 */
    private int frequence;
    /**保存点数*/
    private int savepts;
    /**执行次数*/
    private int donums;			
    /** 任务抄录数据项编码的字符串组合 */
    private String dataCodesStr;
    /** 任务抄录数据项编码列表[String] */
    private List<String> dataCodes=new ArrayList<String>();
    
    
    /** 单位代码 用于按地市保存任务(非初始化属性)*/
    //private String deptCode;
    /** 任务属性,用于区分不同任务表(非初始化属性) */
    //private String taskPlateProperty;
	/** 测量点号(非初始化属性) */
    private String tn;
    /** ct(非初始化属性) */
    //private int ct;
    /** pt(非初始化属性) */
    //private int pt;
    /** 数据保存ID(非初始化属性) */
    //private String dataSaveID;
                   
    /**
     * 从任务配置字符串中解析出终端任务对象。任务配置字符串格式定义（参见浙江规约）如下：
     * 普通任务：TT,TS.NN,TS.UU,TI.NN,TI.UU,RS.NN,RS.UU,RI.NN,RI.UU,RDI,TN,SP,RT,DIN,DI0,DI1,...
     * 中继任务：TT,TS.NN,TS.UU,TI.NN,TI.UU,RS.NN,RS.UU,RI.NN,RI.UU,RDI,PN,PS,SP,WT,CC,GF,GL,CL,CI
     * 异常任务：TT,ALR,TI.NN,TI.UU,TN,DIN,DI0.TN,DI0.DI,...,RT
     * @param s 任务配置字符串
     * @return 终端任务对象
     */
    public static TaskTemplate parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("The task setting could not be null");
        }
        
        String[] values = s.split(DELIM);
        String tt = values[0];
        if (!tt.equals(TASK_TYPE_NORMAL)
                && !tt.equals(TASK_TYPE_FORWARD)) {
            throw new IllegalArgumentException("Invalid task type: " + tt);
        }
        
        TaskTemplate task = new TaskTemplate();
        task.setTaskType(tt);
        if (tt.equals(TASK_TYPE_NORMAL)) {
            setTaskSchedule(task, values);
            task.setTn(values[10]);
            int diCount = Integer.parseInt(values[13]);
            for (int i = 0; i < diCount; i++) {
                task.addDataCode(values[14 + i]);
            }
        }
        else if (tt.equals(TASK_TYPE_FORWARD)) {
            setTaskSchedule(task, values);
        }                
        return task;
    }
    
    /**
     * 设置任务的执行计划
     * @param task 任务对象
     * @param values 属性值数组
     */
    private static void setTaskSchedule(TaskTemplate task, String[] values) {
        task.setSampleStartTime(Integer.parseInt(values[1]));
        task.setSampleStartTimeUnit(values[2]);
        task.setSampleInterval(Integer.parseInt(values[3]));
        task.setSampleIntervalUnit(values[4]);
        task.setUploadStartTime(Integer.parseInt(values[5]));
        task.setUploadStartTimeUnit(values[6]);
        task.setUploadInterval(Integer.parseInt(values[7]));
        task.setUploadIntervalUnit(values[8]);
        task.setFrequence(Integer.parseInt(values[9]));
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        return sb.toString();
    }
    
    /**
     * 添加数据项编码
     * @param code 上报数据项编码
     */
    public void addDataCode(String code) {  
        dataCodes.add(code);
    }
    
    /**
     * 取得任务抄录数据项编码的字符串组合
     * @return
     */
    public String getDataCodesAsString() {
        if (dataCodesStr == null && dataCodes != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dataCodes.size(); i++) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append((String) dataCodes.get(i));
            }
        }
        return dataCodesStr;
    }
    
    
    /**
     * @return Returns the taskType.
     */
    public String getTaskType() {
        return taskType;
    }
    /**
     * @param taskType The taskType to set.
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    /**
     * @return Returns the tn.
     */
    public String getTn() {
        return tn;
    }
    /**
     * @param tn The tn to set.
     */
    public void setTn(String tn) {
        this.tn = tn;
    }   
    
    /**
     * @return Returns the sampleStartTime.
     */
    public int getSampleStartTime() {
        return sampleStartTime;
    }
    /**
     * @param sampleStartTime The sampleStartTime to set.
     */
    public void setSampleStartTime(int sampleStartTime) {
        this.sampleStartTime = sampleStartTime;
    }
    /**
     * @return Returns the sampleStartTimeUnit.
     */
    public String getSampleStartTimeUnit() {
        return sampleStartTimeUnit;
    }
    /**
     * @param sampleStartTimeUnit The sampleStartTimeUnit to set.
     */
    public void setSampleStartTimeUnit(String sampleStartTimeUnit) {
        this.sampleStartTimeUnit = sampleStartTimeUnit;
    }
    /**
     * @return Returns the sampleInterval.
     */
    public int getSampleInterval() {
        return sampleInterval;
    }
    /**
     * @param sampleInterval The sampleInterval to set.
     */
    public void setSampleInterval(int sampleInterval) {
        this.sampleInterval = sampleInterval;
    }
    /**
     * @return Returns the sampleIntervalUnit.
     */
    public String getSampleIntervalUnit() {
        return sampleIntervalUnit;
    }
    /**
     * @param sampleIntervalUnit The sampleIntervalUnit to set.
     */
    public void setSampleIntervalUnit(String sampleIntervalUnit) {
        this.sampleIntervalUnit = sampleIntervalUnit;
    }
    /**
     * @return Returns the uploadStartTime.
     */
    public int getUploadStartTime() {
        return uploadStartTime;
    }
    /**
     * @param uploadStartTime The uploadStartTime to set.
     */
    public void setUploadStartTime(int uploadStartTime) {
        this.uploadStartTime = uploadStartTime;
    }
    /**
     * @return Returns the uploadStartTimeUnit.
     */
    public String getUploadStartTimeUnit() {
        return uploadStartTimeUnit;
    }
    /**
     * @param uploadStartTimeUnit The uploadStartTimeUnit to set.
     */
    public void setUploadStartTimeUnit(String uploadStartTimeUnit) {
        this.uploadStartTimeUnit = uploadStartTimeUnit;
    }
    /**
     * @return Returns the uploadInterval.
     */
    public int getUploadInterval() {
        return uploadInterval;
    }
    /**
     * @param uploadInterval The uploadInterval to set.
     */
    public void setUploadInterval(int uploadInterval) {
        this.uploadInterval = uploadInterval;
    }
    /**
     * @return Returns the uploadIntervalUnit.
     */
    public String getUploadIntervalUnit() {
        return uploadIntervalUnit;
    }
    /**
     * @param uploadIntervalUnit The uploadIntervalUnit to set.
     */
    public void setUploadIntervalUnit(String uploadIntervalUnit) {
        this.uploadIntervalUnit = uploadIntervalUnit;
    }
    /**
     * @return Returns the frequence.
     */
    public int getFrequence() {
        return frequence;
    }
    /**
     * @param frequence The frequence to set.
     */
    public void setFrequence(int frequence) {
        this.frequence = frequence;
    }
    
    /**
     * @return Returns the dataCodes.
     */
    public List<String> getDataCodes() {
        return dataCodes;
    }
    /**
     * @param dataCodes The dataCodes to set.
     */
    public void setDataCodes(List<String> dataCodes) {
        this.dataCodes = dataCodes;
        this.dataCodesStr = null;
    }
   

	

	public int getDonums() {
		return donums;
	}

	public int getSavepts() {
		return savepts;
	}

	
	public void setDonums(int donums) {
		this.donums = donums;
	}

	public void setSavepts(int savepts) {
		this.savepts = savepts;
	}
    
  

	public String toCodeValue(){
    	String desc=null;
    	try{
    		StringBuffer sb=new StringBuffer();
    		if(taskType.equalsIgnoreCase(TASK_TYPE_NORMAL)){
    			sb.append(taskType);
    			sb.append(",");
    			sb.append(sampleStartTimeUnit);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)sampleStartTime));
    			sb.append(",");
    			sb.append(sampleIntervalUnit);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)sampleInterval));
    			sb.append(",");
    			sb.append(uploadStartTimeUnit);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)uploadStartTime));
    			sb.append(",");
    			sb.append(uploadIntervalUnit);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)uploadInterval));
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)frequence));
    			sb.append(",");
    			sb.append(tn);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)savepts));
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)donums));
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)dataCodes.size()));
    			sb.append(",");
    			sb.append(getDataCodesAsString());
    		}
    		desc=sb.toString();
    	}catch(Exception e){
    	}
    	return desc;
    }

	/**
	 * @return 返回 dataCodesStr。
	 */
	public String getDataCodesStr() {
		return dataCodesStr;
	}

	/**
	 * @param dataCodesStr 要设置的 dataCodesStr。
	 */
	public void setDataCodesStr(String dataCodesStr) {
		this.dataCodesStr = dataCodesStr;
	}

	public String getTaskTemplateID() {
		return taskTemplateID;
	}

	public void setTaskTemplateID(String taskTemplateID) {
		this.taskTemplateID = taskTemplateID;
	}	

}
