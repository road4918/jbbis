package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * BCD码的时间YYMMDDHHmmss
 * @author yangdinghuan
 *
 */
public class Parser19 {
	/**
	 * 解析
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析长度
	 * @param fraction 解析后小数位数
	 * @return 数据值
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{			
			boolean ok=true;
			/*if((data[loc] & 0xff)==0xff){
				ok=ParseTool.isValid(data,loc,len);
			}*/
			ok=ParseTool.isValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append("20");
				sb.append(ParseTool.ByteToHex(data[loc+5])); 	//YY
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+4]));	//MM
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//DD
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//mm
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));	//ss
				rt=sb.toString();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return rt;
	}
	
	/**
	 * 组帧
	 * @param frame 存放数据的帧
	 * @param value 数据值(前后不能有多余空字符 YYYY-MM-DD HH:mm:ss)
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c=='-'){
					continue;
				}
				if(c==':'){
					continue;
				}
				if(c==' '){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 YYYY-MM-DD HH:mm:ss 组帧参数:"+value);
			}
			String[] dpara=value.split(" ");
			String[] date=dpara[0].split("-");
			String[] time=dpara[1].split(":");
			
			frame[loc+5]=ParseTool.StringToBcd(date[0]);
			frame[loc+4]=ParseTool.StringToBcd(date[1]);
			frame[loc+3]=ParseTool.StringToBcd(date[2]);
			frame[loc+2]=ParseTool.StringToBcd(time[0]);
			frame[loc+1]=ParseTool.StringToBcd(time[1]);
			frame[loc]=ParseTool.StringToBcd(time[2]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 YYYY-MM-DD HH:mm:ss 组帧参数:"+value);
		}
		
		return len;
	}
}
