package com.hzjbbis.fas.protocol.meter;
/**
 * @filename	MeterParserFactory.java
 * @auther 		netice
 * @date		2006-5-12 15:01:57
 * @version		1.0
 * TODO
 */
public class MeterParserFactory {
	public static IMeterParser getMeterParser(String type){
		IMeterParser rt=null;
		try{
			if(type.equals("ZJMeter")){
				rt=new ZjMeterParser();
			}
			if(type.equals("BBMeter")){
				rt=new BbMeterParser();
			}
			if(type.equals("SMMeter")){
				rt=new SmMeterParser();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return rt;
	}
}
