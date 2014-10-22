package com.hzjbbis.fas.protocol.zj.parse;

import java.util.Arrays;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * 数据项数据编码为字节数据
 * @author yangdinghuan
 *
 */
public class DataItemCoder {
	private static Log log=LogFactory.getLog(DataItemCoder.class);
	
	public static final int DEFAULT_DATABLOCK_MAX=100;	//最大帧数据域长度
	public static final int SMS_DATABLOCK_MAX=210;
	public static final int NET_DATABLOCK_MAX=1000;
	
	
	
	
	/*public static int codeRTUBlock(byte[] frame,int loc,List rtus){
		int rt=0;
		int pos=loc;
		
		frame[pos]=(byte)(rtus.size() & 0xff);
		frame[pos+1]=(byte)((rtus.size() & 0xff00)>>>8);
		rt+=2;
		pos+=2;
		for(Iterator iter=rtus.iterator();iter.hasNext();){
			BusRtu rtu=(BusRtu)iter.next();
			StringBuffer sb=new StringBuffer();
			
			sb.append(String.valueOf(rtu.getRtua()));	//by yangdh ,user rtua???? rtua=A1A2B2B1
			sb.append(",");
			if(rtu.getCommChannel()!=null){
				sb.append(rtu.getCommChannel());
			}else{
				sb.append("02");
			}
			//sb.append("02");	//by yangdh, just for debug
			sb.append(",");
			if(rtu.getCommAddress()!=null){
				sb.append(rtu.getCommAddress());
			}else{
				sb.append("null");
			}
			sb.append(",");
			if(rtu.getManufacturer()!=null){
				sb.append(rtu.getManufacturer());
			}else{
				sb.append("null");
			}
			sb.append(",");
			if(rtu.getPowerVoltage()!=null){
				sb.append(rtu.getPowerVoltage());
			}else{
				sb.append("null");
			}
			sb.append(",");
			if(rtu.getCustomerNo()!=null){
				sb.append(rtu.getCustomerNo());
			}else{
				sb.append("null");
			}
			sb.append(",");
			if(rtu.getCustomerName()!=null){
				sb.append(rtu.getCustomerName());
			}else{
				sb.append("null");
			}
			sb.append(",");
			if(rtu.getStationNo()!=null){
				sb.append(rtu.getStationNo());
			}else{
				sb.append("null");
			}
			sb.append(",");
			if(rtu.getPrincipalMobile()!=null){
				sb.append(rtu.getPrincipalMobile());
			}else{
				sb.append("null");
			}
			
			int len=Parser39.constructor(frame,sb.toString(),pos,71,1);
			if(len<=0){
				log.error("错误的终端参数："+sb.toString());
				return -1;
			}
			rt+=len;
			pos+=len;
		}
		return rt;
	}*/
	
	/**
	 * 假编码---测试用
	 * @param data
	 * @return
	 */
	public static int fakeCoder(byte[] frame,int loc,String data,int len){		
		try{
			Arrays.fill(frame,loc,loc+len-1,(byte)0xff);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return len;
	}
	

	
	
	
	
}
