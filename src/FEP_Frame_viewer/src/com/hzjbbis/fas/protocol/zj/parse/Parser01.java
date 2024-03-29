package com.hzjbbis.fas.protocol.zj.parse;

import java.text.NumberFormat;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.exception.MessageEncodeException;

/**
 * BCD码数值解析和组帧
 * @author yangdinghuan
 *
 */
public class Parser01 {	
	/**
	 * BCD to decimal
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析字节长度
	 * @param fraction 解析后数据包含的小数位数
	 * @return 数据内容
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
				int val=ParseTool.nBcdToDecimal(data,loc,len);
				if(val>=0){
					if(fraction>0){
						NumberFormat snf=NumberFormat.getInstance();
						snf.setMinimumFractionDigits(fraction);
						snf.setGroupingUsed(false);
						rt=snf.format((double)val/ParseTool.fraction[fraction]);
					}else{
						rt=new Integer(val);
					}
				}
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * decimal to bcd
	 * @param frame 字节存放数组
	 * @param value 数据内容
	 * @param loc   存放开始位置
	 * @param len   数据项长度
	 * @param fraction 数据包含小数位数
	 * @return 实际编码长度
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			
			double val=nf.parse(value).doubleValue();
			if(fraction>0){
				val*=ParseTool.fraction[fraction];
			}
			
			ParseTool.IntToBcd(frame,(int)val,loc,len);
		}catch(Exception e){
			//e.printStackTrace();
			throw new MessageEncodeException("bab BCD string:"+value);
		}
		return len;
	}
}
