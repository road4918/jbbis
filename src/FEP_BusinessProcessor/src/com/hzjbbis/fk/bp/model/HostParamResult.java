package com.hzjbbis.fk.bp.model;
/**
 * ��վ�����������ý����
 */
public class HostParamResult {
	/** �ն˾ֺ� */
    private String rtuId;
    /** ������� */
    private String tn;
    /** ������ */
    private String code;
    /** ״̬ */
    private int status;
    /**ʧ��ԭ�� */
    private String sbyy;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public String getSbyy() {
		return sbyy;
	}
	public void setSbyy(String sbyy) {
		this.sbyy = sbyy;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getTn() {
		return tn;
	}
	public void setTn(String tn) {
		this.tn = tn;
	}
}
