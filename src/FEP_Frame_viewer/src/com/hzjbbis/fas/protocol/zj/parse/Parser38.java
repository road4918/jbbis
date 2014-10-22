package com.hzjbbis.fas.protocol.zj.parse;

import java.util.Arrays;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * @filename	Parser38.java
 * @auther 		netice
 * @date		2006-4-5 16:15:08
 * @version		1.0
 * TODO			ascii码字符
 */
public class Parser38 {
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
			rt=Parser43.parsevalue(data,loc,len,fraction);
		}catch(Exception e){
			e.printStackTrace();
		}
		return rt;
	}
	
	/**
	 * 组帧----按规约默认，长度不够时高位填充0x00
	 * @param frame 存放数据的帧
	 * @param value 数据值
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=-1;
		try{
			slen=Parser43.constructor(frame,value,loc,len,fraction);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 ascii码字符 组帧参数:"+value);
		}
		return slen;
	}
}
