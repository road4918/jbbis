package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * BCD字符串 低位在后 高位在前 （小于4字节的解析）
 * @author yangdinghuan
 *
 */
public class Parser18 {
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
				rt=ParseTool.BytesToHexL(data,loc,len);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return rt;
	}
	
	/**
	 * 组帧
	 * @param frame 存放数据的帧
	 * @param value 数据值
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			int nn=Integer.parseInt(value);
			ParseTool.IntToBcdC(frame,nn,loc,len);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 BCD 组帧参数:"+value);
		}
		
		return len;
	}
}
