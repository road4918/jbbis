package com.hzjbbis.fas.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * �ն���������
 */
public class RtuData {
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d HH:mm");
	/** ��λ���� �������񱣴�*/
    //private String deptCode;
	/** ���ݱ���ID */
    //private String dataSaveID;
    /** �ն������ */
    private String taskNum;
    /** �ն������������� */
    //private String taskProperty;
    /** �ն��߼���ַ��HEX�� */
    private String logicAddress;
    /** ������� */
    //private String tn;
    /** ����ʱ�� */
    private Date time;
    /** ct */
    //private int ct;
    /** pt */
    //private int pt;
    
    /** �����б� */
    private List<RtuDataItem> dataList=new ArrayList<RtuDataItem>();


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[logicAddress=").append(getLogicAddress())
        	.append(", taskNum=").append(getTaskNum())
            .append(", time=").append(df.format(getTime())).append("]");
        return sb.toString();
    }
    /**
     * ��Ӹ澯����
     * @param arg �澯����
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
