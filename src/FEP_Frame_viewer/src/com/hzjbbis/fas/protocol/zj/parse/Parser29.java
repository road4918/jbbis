package com.hzjbbis.fas.protocol.zj.parse;

import java.text.NumberFormat;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ģ��������MMYP
 * @author yangdinghuan
 *
 */
public class Parser29 {
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
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//MM
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//Y
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc]));	//P
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
	 * @param value ����ֵ
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c==','){
					continue;
				}				
				if(c>='0' && c<='9'){
					continue;
				}
				if(c>='A' && c<='F'){
					continue;
				}
				if(c>='a' && c<='f'){
					continue;
				}
				throw new MessageEncodeException("����� MM YP ��֡����:"+value);
			}
			String[] para=value.split(",");
			frame[loc]=ParseTool.IntToBcd(Integer.parseInt(para[2]));	//P
			frame[loc+1]=ParseTool.IntToBcd(Integer.parseInt(para[1]));	//Y
			frame[loc+2]=ParseTool.IntToBcd(Integer.parseInt(para[0]));	//MM
		}catch(Exception e){
			throw new MessageEncodeException("����� MM YP ��֡����:"+value);
		}
		
		return len;
	}
}
