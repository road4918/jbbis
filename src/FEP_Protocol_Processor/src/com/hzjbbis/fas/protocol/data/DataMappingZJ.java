package com.hzjbbis.fas.protocol.data;

import java.util.Hashtable;

public class DataMappingZJ implements IMapping{
	//֡���䷽��
	public static final int ORIENTATION_TO_RTU=0;	/*��վ�ٲ�*/
	public static final int ORIENTATION_TO_APP=1;	/*�ն�Ӧ��*/
	//�������
	public static final int ERROR_CODE_OK=0x0;				/*��ȷ���޴���*/
	public static final int ERROR_CODE_NOREP=0x1;			/*�м�����û�з���*/
	public static final int ERROR_CODE_INVALIDCONT=0x2;		/*�������ݷǷ�*/
	public static final int ERROR_CODE_LOWRIGHTS=0x3;		/*Ȩ�޲���*/
	public static final int ERROR_CODE_NOITEM=0x4;			/*��������*/
	public static final int ERROR_CODE_NOTARGET=0x11;		/*Ŀ���ַ������*/
	public static final int ERROR_CODE_SENDFAILUER=0x12;	/*����ʧ��*/
	public static final int ERROR_CODE_SMSLONG=0x13;		/*����Ϣ̫��*/
	
	private Hashtable dataitems;		/*������弯��*/
	
	/**
	 * Ĭ�Ϲ�����
	 *
	 */
	public DataMappingZJ(){
		loadMapping();
	}
	
	/**
	 * �������ݼ���
	 *
	 */
	private void loadMapping(){
		
	}
	
	/**
	 * ȡdataitem
	 */
	public DataItem getDataItem(String key) {
		DataItem rt=null;
		try{
			if(dataitems.containsKey(key)){
				rt=(DataItem)dataitems.get(key);
			}
		}catch(Exception e){
			//
		}
		return rt;
	}
}
