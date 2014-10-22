package com.hzjbbis.fas.protocol.zj.viewer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;
import com.hzjbbis.util.CastorUtil;


/**
 *@filename	DataConfigZj.java
 *@auther	netice
 *@date		2007-7-18
 *@version	1.0
 *TODO		浙规数据项配置集合
 */
public class DataConfigZjpb {
	private static final Log log=LogFactory.getLog(DataConfigZj.class);
	
	private static String DATA_MAP_FILE="com/hzjbbis/fas/protocol/zjpb/conf/protocol-data-config-mapping.xml";
	private static String DATA_CONFIG_FILE="com/hzjbbis/fas/protocol/zjpb/conf/protocol-data-config.xml";
	
	private static DataConfigZjpb _instance;
	private ProtocolDataConfig dataConfig;
	
	
	private DataConfigZjpb(){
		dataIni();
	}
	
	public static DataConfigZjpb getInstance(){
		if(_instance==null){
			synchronized(DataConfigZj.class){
				_instance=new DataConfigZjpb();
			}
		}
		return _instance;
	}
	
	private void dataIni(){
		try{
			dataConfig = (ProtocolDataConfig) CastorUtil.unmarshal(
					DATA_MAP_FILE, DATA_CONFIG_FILE);
	        dataConfig.fillMap();
		}catch(Exception e){
			log.error("data config ini",e);
		}
	}

	public ProtocolDataConfig getDataConfig() {
		return dataConfig;
	}	
}
