package com.hzjbbis.fk.model;

/**
 * �ն�����¼������
 * @author ������
 */
public class TaskTemplateItem {
	/** ����ģ��ID */
    private String taskTemplateID;
    /** ��������� */
    private String code;
    
    public String toString() {
        return "[taskPlateID=" + taskTemplateID + ", code=" + code + "]";
    }
       
    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }
    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
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
}
