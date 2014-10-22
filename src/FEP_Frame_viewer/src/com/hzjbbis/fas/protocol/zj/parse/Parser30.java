package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ����������MMYYPPPPPP
 * @author yangdinghuan
 *
 */
public class Parser30 {
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
				sb.append(ParseTool.ByteToHex(data[loc+4]));	//MM
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//YY
				sb.append(",");
				sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,loc,3)));
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
				throw new MessageEncodeException("����� MM YY PPPPPP ��֡����:"+value);
			}
			String[] para=value.split(",");
			ParseTool.IntToBcd(frame,Integer.parseInt(para[2]),loc,3);
			frame[loc+3]=ParseTool.IntToBcd(Integer.parseInt(para[1]));	//YY
			frame[loc+4]=ParseTool.IntToBcd(Integer.parseInt(para[0]));	//MM
		}catch(Exception e){
			throw new MessageEncodeException("����� MM YY PPPPPP ��֡����:"+value);
		}
		
		return len;
	}
}
