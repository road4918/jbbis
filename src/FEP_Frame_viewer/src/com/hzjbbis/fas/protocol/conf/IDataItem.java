package com.hzjbbis.fas.protocol.conf;

import java.util.List;


/**
 *@filename	IDataItem.java
 *@auther	netice
 *@date		2007-3-15
 *@version	1.0
 *TODO
 */
public interface IDataItem {	
	/**
	 * ���ض�Ӧ��׼���ݼ����������
	 */
	public abstract List getStandardDatas();
	
	/**
	 * ����ת��Ϊ��׼���ݼ��Ĺ�����key
	 */
	public abstract String getSdRobot();
	
	/**
	 * ��׼�������Ƿ�����ڴ���������
	 * @param dataid	��׼���ݼ���������id
	 */
	public abstract boolean isMe(String dataid);
}
