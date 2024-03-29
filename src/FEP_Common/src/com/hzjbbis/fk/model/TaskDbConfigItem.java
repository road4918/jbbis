package com.hzjbbis.fk.model;

import java.util.ArrayList;

import sun.util.logging.resources.logging;



/**
 * 任务数据项数据库保存信息
 */
public class TaskDbConfigItem {   
    /** 数据库表名 */
    private String tableName;
    /** 数据库表字段名 */
    private String fieldName;
    /** 特殊处理标记:00只保存零点数据;01表示只保存整点数据 */
    private String tag;
    /** 任务属性列表：范例‘02,01’(只有匹配的任务属性才能保存) */
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
