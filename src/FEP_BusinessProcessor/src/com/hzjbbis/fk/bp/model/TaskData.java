package com.hzjbbis.fk.bp.model;

import java.util.Date;
/**
 *���ݿ�ӳ���: �̱�������
 */
public class TaskData {
	/** ���ݱ���ID */
	private String SJID;
    /** ����ʱ�� */
    private Date SJSJ;
    /** CT */
    private int CT;
    /** PT */
    private int PT;	
    /** ��ȫ��־ */
    private int BQBJ;
    /** ���ݱ��� */
	private String SJBH;
	/** ����ֵ*/
	private String SJZ;
	
	public int getBQBJ() {
		return BQBJ;
	}
	public void setBQBJ(int bqbj) {
		BQBJ = bqbj;
	}
	public int getCT() {
		return CT;
	}
	public void setCT(int ct) {
		CT = ct;
	}
	public int getPT() {
		return PT;
	}
	public void setPT(int pt) {
		PT = pt;
	}
	public String getSJBH() {
		return SJBH;
	}
	public void setSJBH(String sjbh) {
		SJBH = sjbh;
	}
	public String getSJID() {
		return SJID;
	}
	public void setSJID(String sjid) {
		SJID = sjid;
	}
	public Date getSJSJ() {
		return SJSJ;
	}
	public void setSJSJ(Date sjsj) {
		SJSJ = sjsj;
	}
	public String getSJZ() {
		return SJZ;
	}
	public void setSJZ(String sjz) {
		SJZ = sjz;
	}	
}
