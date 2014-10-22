package com.hzjbbis.fas.model;

import java.io.Serializable;
import java.util.Map;


/**
 * FAAL ͨѶ��Ӧ
 */
public class FaalRequestResponse implements Serializable{
	private static final long serialVersionUID = 1L;
	
    /** ��վ���������Ӧ������ID */
    private Long cmdId;
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** FAAL ͨѶ�������� */
    private String cmdStatus;
    /** FAAL ͨѶ���󷵻ز������ */
    private Map<String,String> params;
    
	public Long getCmdId() {
		return cmdId;
	}
	public void setCmdId(Long cmdId) {
		this.cmdId = cmdId;
	}
	public String getCmdStatus() {
		return cmdStatus;
	}
	public void setCmdStatus(String cmdStatus) {
		this.cmdStatus = cmdStatus;
	}
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

       
}
