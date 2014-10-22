package com.hzjbbis.fas.protocol.meter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.data.DataItem;


/**
 *@filename	SmMeterParser.java
 *@auther	netice
 *@date		2007-3-4
 *@version	1.0
 *TODO		西门子表规
 */
public class SmMeterParser implements IMeterParser{
	private final Log log=LogFactory.getLog(SmMeterParser.class);
	
	public byte[] constructor(String[] datakey, DataItem para) {		
		//西门子表规一次召回所有数据
		return new byte[]{0x2F,0x3F,0x21,0x0D,0x0A};
	}

	public String[] convertDataKey(String[] datakey) {		
		//西门子表规暂不需要转换
		return datakey;
	}

	public Object[] parser(byte[] data, int loc, int len) {
		List result=null;
		
		try{
			SmMeterFrame frame=new SmMeterFrame();
			frame.parse(data, loc, len);
			if(frame.getLen()>0){
				//包含有效数据
				String dbuf=new String(frame.getData(),"iso-8859-1");	//按英文字符解析
				result=new ArrayList();
				int sindex=0;
				int eindex=0;
				
				sindex=dbuf.indexOf("4.1(");
				if(sindex>=0){//包含 正向有功（峰）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9011",result);
					}
				}
				sindex=dbuf.indexOf("4.2(");
				if(sindex>=0){//包含 正向有功（平）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9012",result);
					}
				}
				sindex=dbuf.indexOf("4.3(");
				if(sindex>=0){//包含 正向有功（谷）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9013",result);
					}
				}
				
				sindex=dbuf.indexOf("5.1(");
				if(sindex>=0){//包含 反向有功（峰）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9021",result);
					}
				}
				sindex=dbuf.indexOf("5.2(");
				if(sindex>=0){//包含 反向有功（平）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9022",result);
					}
				}
				sindex=dbuf.indexOf("5.3(");
				if(sindex>=0){//包含 反向有功（谷）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9023",result);
					}
				}
				
				sindex=dbuf.indexOf("6(");
				if(sindex>=0){//包含 正向有功（总）
					sindex+=2;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9010",result);
					}
				}
				
				sindex=dbuf.indexOf("7(");
				if(sindex>=0){//包含 反向有功（总）
					sindex+=2;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9020",result);
					}
				}
				
				sindex=dbuf.indexOf("8(");
				if(sindex>=0){//包含 正向无功（总）
					sindex+=2;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9110",result);
					}
				}
				sindex=dbuf.indexOf("9(");
				if(sindex>=0){//包含 反向无功（总）
					sindex+=2;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"9120",result);
					}
				}
				
				sindex=dbuf.indexOf("12(");
				if(sindex>=0){//包含 正向有功总最大需量和发生时间
					sindex+=3;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"A010",result);
						
						if(dbuf.substring(eindex+1, eindex+2).equals("(")){//发生时间
							sindex=eindex+2;	//数据开始位置
							eindex=dbuf.indexOf(")",sindex+1);
							if(eindex>0){
								dstring=dbuf.substring(sindex, eindex);
								addItem("20"+dstring,"B010",result);
							}
						}
					}					
				}
				
				sindex=dbuf.indexOf("13(");
				if(sindex>=0){//包含 反向有功总最大需量和发生时间
					sindex+=3;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"A020",result);
						
						if(dbuf.substring(eindex+1, eindex+2).equals("(")){//发生时间
							sindex=eindex+2;	//数据开始位置
							eindex=dbuf.indexOf(")",sindex+1);
							if(eindex>0){
								dstring=dbuf.substring(sindex, eindex);
								addItem("20"+dstring,"B020",result);
							}
						}
					}					
				}
				
				sindex=dbuf.indexOf("L.1(");
				if(sindex>=0){//包含 反向无功（总）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"B611",result);
					}
				}
				sindex=dbuf.indexOf("L.2(");
				if(sindex>=0){//包含 反向无功（总）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"B612",result);
					}
				}
				sindex=dbuf.indexOf("L.3(");
				if(sindex>=0){//包含 反向无功（总）
					sindex+=4;//数据开始位置
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//数据完整
						String dstring=dbuf.substring(sindex, eindex);
						//解析数据，数据字串中可能有单位
						String val=fixValue(dstring);
						addItem(val,"B613",result);
					}
				}
			}
		}catch(Exception e){
			log.error("解析西门子表规约", e);
		}
		
		if(result!=null){
			return result.toArray();
		}
		return null;
	}

	private String fixValue(String val){
		int index=val.indexOf("*");
		if(index>0){
			return val.substring(0, index);
		}
		return val;
	}
	
	private void addItem(String val,String key,List result){
		DataItem item=new DataItem();
		item.addProperty("value",val);
		item.addProperty("datakey",key);
		result.add(item);
	}
}
