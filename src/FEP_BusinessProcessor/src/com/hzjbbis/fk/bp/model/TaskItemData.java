package com.hzjbbis.fk.bp.model;

import java.util.Date;
/**
 * ���������
 * ���ڵ��ô洢����pkg_fep_service.sbsjmxb_ins
 */
public class TaskItemData {
	/** ��λ���� */
    private String deptCode;
    /** ���� */
    private String customerNo;
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ������ֺ� */
    private String stationNo;    
    /** ���ݱ��� */
	private String code;
	/** ����ֵ*/
	private String value;
	/** ͨѶ��ʽ*/
	private String txfs;
    /** ����ʱ�� */
    private Date time;
	/** CT */
    private int ct;
    /** PT */
    private int pt;
    
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getCt() {
		return ct;
	}
	public void setCt(int ct) {
		this.ct = ct;
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
	public int getPt() {
		return pt;
	}
	public void setPt(int pt) {
		this.pt = pt;
	}
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public String getStationNo() {
		return stationNo;
	}
	public void setStationNo(String stationNo) {
		this.stationNo = stationNo;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getTxfs() {
		return txfs;
	}
	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}	
}
