package com.hzjbbis.fas.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 终端任务数据
 */
public class RtuData {
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d HH:mm");
	/** 单位代码 用于任务保存*/
    //private String deptCode;
	/** 数据保存ID */
    //private String dataSaveID;
    /** 终端任务号 */
    private String taskNum;
    /** 终端任务数据属性 */
    //private String taskProperty;
    /** 终端逻辑地址（HEX） */
    private String logicAddress;
    /** 测量点号 */
    //private String tn;
    /** 数据时间 */
    private Date time;
    /** ct */
    //private int ct;
    /** pt */
    //private int pt;
    
    /** 数据列表 */
    private List<RtuDataItem> dataList=new ArrayList<RtuDataItem>();


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[logicAddress=").append(getLogicAddress())
        	.append(", taskNum=").append(getTaskNum())
            .append(", time=").append(df.format(getTime())).append("]");
        return sb.toString();
    }
    /**
     * 添加告警参数
     * @param arg 告警参数
     */
    public void addDataList(RtuDataItem rtuDataItem) {
    	dataList.add(rtuDataItem);
    }

	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public String getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(String taskNum) {
		this.taskNum = taskNum;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public List<RtuDataItem> getDataList() {
		return dataList;
	}
	
}
