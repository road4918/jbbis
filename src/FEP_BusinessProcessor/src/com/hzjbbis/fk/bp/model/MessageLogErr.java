package com.hzjbbis.fk.bp.model;

import java.util.Date;
/**
 * 非法报文日志表映射类
 */
public class MessageLogErr {
	/** 终端逻辑地址 */
    private String logicAddress;
    /** 区域码(终端逻辑地址前两位) */
    private String qym;
    /** 控制码 */
    private String kzm;
    /** 通讯时间 */
    private Date time;   
    /** 原始报文 */
    private String body;
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getKzm() {
		return kzm;
	}
	public void setKzm(String kzm) {
		this.kzm = kzm;
	}
	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public String getQym() {
		return qym;
	}
	public void setQym(String qym) {
		this.qym = qym;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
}
