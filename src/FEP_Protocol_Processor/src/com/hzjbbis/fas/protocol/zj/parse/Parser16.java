package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ����
 * @author yangdinghuan
 *
 */
public class Parser16 {
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
				int port=ParseTool.nByteToInt(data,loc,2);	//net port					
				String ip=(data[loc+5] & 0xff)+"."+(data[loc+4] & 0xff)
					+"."+(data[loc+3] & 0xff)+"."+(data[loc+2] & 0xff);					
				rt=ip+":"+port;
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
			ParseTool.IPToBytes(frame,loc,value);
			frame[loc+6]=(byte)0xAA;
			frame[loc+7]=(byte)0xAA;
		}catch(Exception e){
			throw new MessageEncodeException("����� ���� ��֡����:"+value);
		}
		return len;
	}
}
