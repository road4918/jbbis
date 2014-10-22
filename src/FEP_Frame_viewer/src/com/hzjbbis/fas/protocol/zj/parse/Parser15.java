package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * �������ĺ���
 * @author yangdinghuan
 *
 */
public class Parser15 {
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
				rt=ParseTool.toPhoneCode(data,loc,len,0xAA);
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
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� �������ĺ��� ��֡����:"+value);
			}
			ParseTool.StringToBcds(frame,loc,value);
			int flen=((value.length()>>>1)+(value.length() & 0x01));
			for(int i=loc+flen;i<loc+len;i++){
				frame[i]=(byte)0xAA;
			}			
		}catch(Exception e){
			throw new MessageEncodeException("����� �������ĺ��� ��֡����:"+value);
		}
		return len;
	}
}
