package com.hzjbbis.fk.model;


/**
 * �ն�������Ϣ
 */
public class RtuTask {   
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ����ģ��ID */
    private String taskTemplateID;
    /** ����ģ������ */
    private String taskTemplateProperty;
    /** �ն������ */
    private int rtuTaskNum;
    /** ������� */
    private String tn;
	
	/**
	 * @return ���� rtuId��
	 */
	public String getRtuId() {
		return rtuId;
	}
	/**
	 * @param rtuId Ҫ���õ� rtuId��
	 */
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	/**
	 * @return ���� rtuaTaskNum��
	 */
	public int getRtuTaskNum() {
		return rtuTaskNum;
	}
	/**
	 * @param rtuaTaskNum Ҫ���õ� rtuaTaskNum��
	 */
	public void setRtuTaskNum(int rtuaTaskNum) {
		this.rtuTaskNum = rtuaTaskNum;
	}
	/**
	 * @return ���� taskPlateID��
	 */
	public String getTaskTemplateID() {
		return taskTemplateID;
	}
	/**
	 * @param taskPlateID Ҫ���õ� taskPlateID��
	 */
	public void setTaskTemplateID(String taskTemplateID) {
		this.taskTemplateID = taskTemplateID;
	}
	
	
	public String getTaskTemplateProperty() {
		return taskTemplateProperty;
	}
	public void setTaskTemplateProperty(String taskTemplateProperty) {
		this.taskTemplateProperty = taskTemplateProperty;
	}
	/**
	 * @return ���� tn��
	 */
	public String getTn() {
		return tn;
	}
	/**
	 * @param tn Ҫ���õ� tn��
	 */
	public void setTn(String tn) {
		this.tn = tn;
	}
    
	
}
