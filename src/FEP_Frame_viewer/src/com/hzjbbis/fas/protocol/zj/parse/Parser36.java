package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * @filename	Parser36.java
 * @auther 		netice
 * @date		2006-4-5 15:39:10
 * @version		1.0
 * TODO			A1A2B2B1
 */
public class Parser36 {
	/**
	 * ����
	 * @param data ����֡
	 * @param loc  ������ʼλ��
	 * @param len  ��������
	 * @param fraction ������С��λ��
	 * @return ����ֵ
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{			
			boolean ok=true;
			/*if((data[loc] & 0xff)==0xff){
				ok=ParseTool.isValid(data,loc,len);
			}*/
			ok=ParseTool.isValidBCD(data,loc,len);
			if(len!=4){
				ok=false;
			}
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.BytesToHexL(data,loc,2));
				sb.append(ParseTool.BytesToHexL(data,loc+2,2));				
				rt=sb.toString();				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return rt;
	}
	
	/**
	 * ��֡
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ	A1A2B2B1
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			ParseTool.HexsToBytesCB(frame,loc,value.substring(0,4));
			ParseTool.HexsToBytesCB(frame,loc+2,value.substring(4,8));
		}catch(Exception e){
			throw new MessageEncodeException("����� A1A2B2B1 ��֡����:"+value);
		}
		return len;
	}
}
