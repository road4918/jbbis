package com.hzjbbis.fk.bp.model;

import java.util.Date;
/**
 * �Ƿ�������־��ӳ����
 */
public class MessageLogErr {
	/** �ն��߼���ַ */
    private String logicAddress;
    /** ������(�ն��߼���ַǰ��λ) */
    private String qym;
    /** ������ */
    private String kzm;
    /** ͨѶʱ�� */
    private Date time;   
    /** ԭʼ���� */
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
