package com.hzjbbis.fk.model;

import java.util.ArrayList;


/**
 * �������������ݿⱣ����Ϣ
 */
public class TaskDbConfig {   
	/** ��������� */
    private String code;
    /** ���ݿ����+�ֶ���+���⴦�����ִ�*/
    private String dbConfigStr;
    /** ���ݿ����+�ֶ���+���⴦����:TaskDbConfigItem */
    private ArrayList<TaskDbConfigItem> taskDbConfigItemList=new ArrayList<TaskDbConfigItem>();
    
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public void addTaskDbConfigItemList(String[] s){
		TaskDbConfigItem tsti= new TaskDbConfigItem() ;
		if (s.length==4){
			tsti.setTableName(s[0]);
			tsti.setFieldName(s[1]);
			tsti.setTag(s[2]);
			tsti.setTaskPropertyStr(s[3]);
			this.taskDbConfigItemList.add(tsti);
		}			
	}
	
	public void setTaskDbConfigItemList(String dbConfigStr) {
		String[] s=dbConfigStr.split("/");
		for(int i=0;i<s.length;i++){
			String[] ss=s[i].split(";");
			addTaskDbConfigItemList(ss);
		}
	}

	public String getDbConfigStr() {
		return dbConfigStr;
	}

	public void setDbConfigStr(String dbConfigStr) {
		this.dbConfigStr = dbConfigStr;
		setTaskDbConfigItemList(dbConfigStr);
	}

	public ArrayList<TaskDbConfigItem> getTaskDbConfigItemList() {
		return taskDbConfigItemList;
	}	
}
