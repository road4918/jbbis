package com.hzjbbis.fk.bp.model;

import java.util.Date;
/**
 * �ն����������Ϣ��ӳ����
 */
public class RtuCommandIdInfo {
	/** �ն��߼���ַ */
    private String logicAddress;
    /** ֡��� */
    private int zxh;
	/** ����ID */
    private Long cmdId;
    /** �������� */
    private int bwsl;
    /** �Զ�װ�ӱ�־ */
    private int zdzjbz;
    /** ����ʱ�� */
    private Date time;

	public Long getCmdId() {
		return cmdId;
	}
	public void setCmdId(Long cmdId) {
		this.cmdId = cmdId;
	}
	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public int getZxh() {
		return zxh;
	}
	public void setZxh(int zxh) {
		this.zxh = zxh;
	}
	public int getBwsl() {
		return bwsl;
	}
	public void setBwsl(int bwsl) {
		this.bwsl = bwsl;
	}
	public int getZdzjbz() {
		return zdzjbz;
	}
	public void setZdzjbz(int zdzjbz) {
		this.zdzjbz = zdzjbz;
	}
}
