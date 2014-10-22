package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * HEX���ִ���������֡ ��λ��ǰ ��λ�ں�
 * @author yangdinghuan
 *
 */
public class Parser06 {
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
			//ok=ParseTool.isAllFF(data,loc,len);
			if(ok){
				rt=ParseTool.BytesToHexL(data,loc,len);
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
			ParseTool.HexsToBytesCB(frame,loc,value);
		}catch(Exception e){
			throw new MessageEncodeException("�����HEX����֡����:"+value);
		}
		return len;
	}
}
