package com.hzjbbis.fas.protocol.conf;


/**
 *@filename	IDataSets.java
 *@auther	netice
 *@date		2007-3-15
 *@version	1.0
 *TODO
 */
public interface IDataSets {
	/**
	 * ȡ��������
	 * @param key	��������key	 
	 * @return
	 */
	public abstract IItemParser getParser(String key);
	
	/**
	 * ȡ�������ݱ�ʶ
	 * @param key	��׼���ݱ�ʶ
	 * @param para	����	DataItem��
	 * @return
	 */
	public abstract String getLocal(String key,Object para);	
}
