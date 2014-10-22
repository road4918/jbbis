package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ʱ���������֡ MM-DD
 * @author yangdinghuan
 *
 */
public class Parser41 {
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
				sb.append(ParseTool.ByteToHex(data[loc+1]));
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc]));				
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
	 * @param value ����ֵ(ǰ�����ж�����ַ�)
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
				if(c=='-'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}
				throw new MessageEncodeException("����� MM-DD ��֡����:"+value);
			}
			String[] date=value.split("-");			
			frame[loc]=ParseTool.StringToBcd(date[1]);
			frame[loc+1]=ParseTool.StringToBcd(date[0]);
		}catch(Exception e){
			throw new MessageEncodeException("����� MM-DD ��֡����:"+value);
		}
		return len;
	}
}
