package com.hzjbbis.fas.protocol.data;

import java.util.Hashtable;

public class DataItem {
	private Hashtable property;
	
	public DataItem(){
		property=new Hashtable();
	}
	
	public Hashtable getPropertys(){
		return property;
	}
	
	/**
	 * ȡ����������
	 * @param key �������У�value��ֵ�� point�������㣩 des������������ datakey�����ݱ�ʶ��
	 * @return
	 */
	public Object getProperty(String key){
		if(property!=null){
			if(property.containsKey(key)){
				return property.get(key);
			}
		}
		return null;
	}
	
	public void addProperty(String key,Object value){
		try{
			if(property!=null){
				if(property.containsKey(key)){
					property.remove(key);					
				}
				property.put(key,value);
			}
		}catch(Exception e){
			//
		}
	}
	
	public String toString(){
		return (String)property.get("des");
	}
}
