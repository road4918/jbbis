package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * BCD码表示的时间HHmm
 * @author yangdinghuan
 *
 */
public class Parser13 {
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
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//hh
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));	//mm					
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
	 * @param value 数据值(前后不能有多余空字符 HH:MM,NN)
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
				if(c==':'){
					continue;
				}				
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 HH:mm 组帧参数:"+value);
			}
			String[] time=value.split(":");
			frame[loc+1]=ParseTool.StringToBcd(time[0]);
			frame[loc]=ParseTool.StringToBcd(time[1]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 hh:mm 组帧参数:"+value);
		}
		
		return len;
	}
}
