package com.hzjbbis.fas.protocol.zj.parse;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.hzjbbis.fas.model.FaalRequestParam;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;

import com.hzjbbis.fk.model.BizRtu;

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
	
	
	
	public static int coder(byte[] frame,int loc,FaalRequestParam fp,ProtocolDataItemConfig dic){		
		int slen=-1;
		try{
			if(frame!=null){
				if(fp!=null && dic!=null){
					String value=fp.getValue();
					switch(dic.getParserno()){
						case 1:
							slen=Parser01.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 2:
							slen=Parser02.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 3:
							slen=Parser03.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 4:
							slen=Parser04.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 5:
							slen=Parser05.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 6:
							slen=Parser06.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 7:
							slen=Parser07.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 8:
							slen=Parser08.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 9:
							slen=Parser09.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 10:
							slen=Parser10.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 11:
							slen=Parser11.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 12:
							slen=Parser12.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 13:
							slen=Parser13.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 14:
							slen=Parser14.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 15:
							slen=Parser15.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 16:
							slen=Parser16.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 17:
							slen=Parser17.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 18:
							slen=Parser18.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 19:
							slen=Parser19.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 20:
							slen=Parser20.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 21:
							slen=Parser21.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 22:
							slen=Parser22.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 23:
							slen=Parser23.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 24:
							slen=Parser24.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 25:
							slen=Parser25.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 26:
							slen=Parser26.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 27:
							slen=Parser27.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 28:
							slen=Parser28.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 29:
							slen=Parser29.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 30:
							slen=Parser30.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 31:
							slen=Parser31.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 32:
							slen=Parser32.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 33:
							slen=Parser33.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 34:
							slen=Parser34.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 35:
							slen=Parser35.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 36:
							slen=Parser36.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;	
						case 37:
							slen=Parser37.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;	
						case 38:
							slen=Parser38.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;	
						case 39:
							slen=Parser39.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;	
						case 40:
							slen=Parser40.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;	
						case 41:
							slen=Parser41.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;	
						case 42:
							slen=Parser42.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;	
						case 43:
							slen=Parser43.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;	
						case 44:
							slen=Parser44.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 45:
							slen=Parser45.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 46:
							slen=Parser46.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 47:
							slen=Parser47.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 48:
							slen=Parser48.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 49:
							slen=Parser49.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 50:
							slen=Parser50.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 51:
							slen=Parser51.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 52:
							slen=Parser52.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 53:
							slen=Parser53.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;
						case 54:
							slen=Parser54.constructor(frame,value,loc,dic.getLength(),dic.getFraction());
							break;						
						default:
							slen=fakeCoder(frame,loc,value,dic.getLength());
							break;						
					}
				}else{
					//主站传入参数错误
					
				}
			}else{
				//没有组帧空间
			}			
		}catch(Exception e){
			log.error("coder",e);
		}
		return slen;
	}
	
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
	
	public static int getDataMax(BizRtu rtu){
		return DEFAULT_DATABLOCK_MAX;
	}
	
	
	
	
}
