package com.hzjbbis.fk.bp.model;

import java.util.Date;

/**
 * �ն�����/������Ϣ��ӳ����
 */
public class MessageLog {
	/** �ն��߼���ַ */
    private String logicAddress;
    /** ������(�ն��߼���ַǰ��λ) */
    private String qym;
    /** ������ */
    private String kzm;
    /** Դ��ַ */
    private String srcAddr;
    /** Ŀ���ַ */
    private String destAddr;
    /** ͨѶ��ʽ */
    private String txfs;
    /** ͨѶʱ�� */
    private Date time;   
    /** ���Ĵ�С */
    private int size;
    /** ԭʼ���� */
    private String body;

    
	public String getBody() {
		return body;
	}


	public void setBody(String body) {
		this.body = body;
	}


	public String getDestAddr() {
		return destAddr;
	}


	public void setDestAddr(String destAddr) {
		this.destAddr = destAddr;
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


	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}


	public String getSrcAddr() {
		return srcAddr;
	}


	public void setSrcAddr(String srcAddr) {
		this.srcAddr = srcAddr;
	}


	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}


	public String getTxfs() {
		return txfs;
	}


	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}


	public String toString() {
		return "rtua="+logicAddress + ",message="+body;
	}
}
