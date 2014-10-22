package com.hzjbbis.fas.protocol.meter;

import com.hzjbbis.fas.protocol.data.DataItem;

public interface IMeterParser {
	/**
	 * ת�����ݱ�ʶ
	 * @param datakey  �㽭��Լ���������ݱ�ʶ
	 * @return
	 */
	public String[] convertDataKey(String[] datakey);
	
	/**
	 * ����Լ�����ٲ�֡
	 * @param datakey  ���Լ���ݱ�ʶ
	 * @param para	   ��������	
	 * @return
	 */
	public byte[] constructor(String[] datakey,DataItem para);
	
	/**
	 * ������֡
	 * @param data
	 * @return
	 */
	public Object[] parser(byte[] data,int loc,int len);
}
