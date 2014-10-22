package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * @filename	Parser45.java
 * @auther 		netice
 * @date		2006-5-13 10:41:13
 * @version		1.0
 * TODO
 */
public class Parser45 {
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
			rt=ParseTool.BytesBitC(data,loc,len);
		}catch(Exception e){
			e.printStackTrace();
		}
		return rt;
	}
	
	/**
	 * ��֡----����ԼĬ�ϣ����Ȳ���ʱ��λ���0x00
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=-1;
		try{
			int vlen=value.length();
			for(int i=0;i<vlen;i++){
				if((value.substring(i,i+1).equals("0")) || (value.substring(i,i+1).equals("1"))){
					//
				}else{
					throw new MessageEncodeException("����� bitλ�� ��֡����:"+value);
				}
			}
			if((vlen & 0x7)==0){//��8����������
				int blen=0;
				int iloc=loc;
				while(blen<vlen){
					frame[iloc]=ParseTool.bitToByteC(value.substring(blen,blen+8));
					blen+=8;
					iloc++;
				}
				slen=len;
			}
		}catch(Exception e){
			throw new MessageEncodeException("����� bitλ�� ��֡����:"+value);
		}
		return slen;
	}
}
