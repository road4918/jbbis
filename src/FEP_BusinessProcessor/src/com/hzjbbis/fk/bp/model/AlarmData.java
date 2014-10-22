package com.hzjbbis.fk.bp.model;

import java.util.Date;

/**
 * �ն��ϱ��澯��
 * 1�����ϱ��澯��ӳ��
 * 2���ڵ��ô洢����pkg_fep_service.sb_gj_ins
 */
public class AlarmData {
    /** ���ݱ���ID */
    private long dataSaveID;
    /** ��λ���� */
    private String deptCode;
    /** ���� */
    private String customerNo;
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ������ֺ� */
    private String stationNo;
    /** �澯���루ʮ�������ַ����� */
    private String alertCodeHex;
    /** �澯����ʱ�� */
    private Date alertTime;
    /** �澯����ʱ�� */
    private Date receiveTime;
    /** �澯��������*/
    private String sbcs;
    /** ͨѶ��ʽ */
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
