package com.hzjbbis.fas.protocol.data;

import java.util.ArrayList;
import java.util.List;

public class RTUReply {
	private int type;	/*Ӧ������*/
	private List data;	/*Ӧ������*/
	private String des;
	
	public RTUReply(){
		this(0,null);
	}
	
	public RTUReply(int type,List data){
		this.type=type;
		this.data=data;
	}
	
	/**
	 * ���������
	 * @param item
	 */
	public void addDataItem(DataItem item){
		if(data==null){
			data=new ArrayList();
		}
		data.add(item);
	}
	
	/**
	 * @return Returns the data.
	 */
	public List getData() {
		return data;
	}


	/**
	 * @param data The data to set.
	 */
	public void setData(List data) {
		this.data = data;
	}


	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}


	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}


	public String toString(){
		return des;
	}
}
