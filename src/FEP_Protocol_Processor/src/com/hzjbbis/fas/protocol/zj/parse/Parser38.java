package com.hzjbbis.fas.protocol.zj.parse;

import java.util.Arrays;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * @filename	Parser38.java
 * @auther 		netice
 * @date		2006-4-5 16:15:08
 * @version		1.0
 * TODO			ascii���ַ�
 */
public class Parser38 {
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
			rt=Parser43.parsevalue(data,loc,len,fraction);
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
			slen=Parser43.constructor(frame,value,loc,len,fraction);
		}catch(Exception e){
			throw new MessageEncodeException("����� ascii���ַ� ��֡����:"+value);
		}
		return slen;
	}
}
