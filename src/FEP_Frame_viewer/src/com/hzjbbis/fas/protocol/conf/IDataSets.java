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
	 * 取解析工具
	 * @param key	解析工具key	 
	 * @return
	 */
	public abstract IItemParser getParser(String key);
	
	
}
