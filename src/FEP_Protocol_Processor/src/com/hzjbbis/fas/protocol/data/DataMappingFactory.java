package com.hzjbbis.fas.protocol.data;

import java.util.Hashtable;

/**
 * ���ݼ��Ϲ���---������ͬ��Լ�����ݼ���
 * @author yangdh
 * @version 
 */
public class DataMappingFactory {
	private static Hashtable datamappings;
	private static Object lock=new Object();
	
	/**
	 * ��������
	 * @param key
	 * @return
	 */
	public static IMapping createDataMapping(String key){
		synchronized(lock){
			if(datamappings==null){
				datamappings=new Hashtable();
			}
			if(!datamappings.containsKey(key)){			
				//�������ݼ���
				datamappings.put(key,createMapping(key));				
			}
			return (IMapping)datamappings.get(key);
		}
	}
	
	/**
	 * �½����ݼ���
	 * @param key
	 * @return
	 */
	private static IMapping createMapping(String key){
		if(key.equals("ZJ")){
			return new DataMappingZJ();
		}
		return null;
	}
}
