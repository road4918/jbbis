package com.hzjbbis.fk.bp.model;

import java.util.Date;

/**
 * 终端上报告警类
 * 1用于上报告警表映射
 * 2用于调用存储过程pkg_fep_service.sb_gj_ins
 */
public class AlarmData {
    /** 数据保存ID */
    private long dataSaveID;
    /** 单位代码 */
    private String deptCode;
    /** 户号 */
    private String customerNo;
    /** 终端局号ID */
    private String rtuId;
    /** 测量点局号 */
    private String stationNo;
    /** 告警编码（十六进制字符串） */
    private String alertCodeHex;
    /** 告警发生时间 */
    private Date alertTime;
    /** 告警接收时间 */
    private Date receiveTime;
    /** 告警附加数据*/
    private String sbcs;
    /** 通讯方式 */
    private String txfs;

	public String getTxfs() {
		return txfs;
	}
	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}
	public String getAlertCodeHex() {
		return alertCodeHex;
	}
	public void setAlertCodeHex(String alertCodeHex) {
		this.alertCodeHex = alertCodeHex;
	}
	public Date getAlertTime() {
		return alertTime;
	}
	public void setAlertTime(Date alertTime) {
		this.alertTime = alertTime;
	}
	public String getCustomerNo() {
		return customerNo;
	}
	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}
	public String getDeptCode() {
		return deptCode;
	}
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	public Date getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public String getSbcs() {
		return sbcs;
	}
	public void setSbcs(String sbcs) {
		if (sbcs==null)
			sbcs="";
		this.sbcs = sbcs;
	}
	public String getStationNo() {
		return stationNo;
	}
	public void setStationNo(String stationNo) {
		this.stationNo = stationNo;
	}
	public long getDataSaveID() {
		return dataSaveID;
	}
	public void setDataSaveID(long dataSaveID) {
		this.dataSaveID = dataSaveID;
	}
    
}
