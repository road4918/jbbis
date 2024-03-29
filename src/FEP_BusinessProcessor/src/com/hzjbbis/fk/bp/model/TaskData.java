package com.hzjbbis.fk.bp.model;

import java.util.Date;
/**
 *数据库映射表: 短表任务类
 */
public class TaskData {
	/** 数据保存ID */
	private String SJID;
    /** 数据时间 */
    private Date SJSJ;
    /** CT */
    private int CT;
    /** PT */
    private int PT;	
    /** 补全标志 */
    private int BQBJ;
    /** 数据编码 */
	private String SJBH;
	/** 数据值*/
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
