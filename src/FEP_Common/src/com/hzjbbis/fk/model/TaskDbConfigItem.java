package com.hzjbbis.fk.model;

import java.util.ArrayList;

import sun.util.logging.resources.logging;



/**
 * �������������ݿⱣ����Ϣ
 */
public class TaskDbConfigItem {   
    /** ���ݿ���� */
    private String tableName;
    /** ���ݿ���ֶ��� */
    private String fieldName;
    /** ���⴦����:00ֻ�����������;01��ʾֻ������������ */
    private String tag;
    /** ���������б�������02,01��(ֻ��ƥ����������Բ��ܱ���) */
    private String taskPropertyStr;
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getTaskPropertyStr() {
		return taskPropertyStr;
	}
	public void setTaskPropertyStr(String taskPropertyStr) {
		this.taskPropertyStr = taskPropertyStr;
	}
    public boolean taskPropertyContains(String taskProperty){
    	if (taskPropertyStr.indexOf(taskProperty)>-1)
    		return true;
    	else 
    		return false;
    }
	
	
    
	
	
    
	
}
