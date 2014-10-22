package com.hzjbbis.fas.protocol.zj.parse;

import java.text.NumberFormat;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.exception.MessageEncodeException;

public class Parser49 {
	/**
	 * 18��01����
	 * @param data ����֡
	 * @param loc  ������ʼλ��
	 * @param len  �����ֽڳ���
	 * @param fraction ���������ݰ�����С��λ��
	 * @return ��������
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{
			boolean ok=true;
			int pos=loc;
			if(ok){
				StringBuffer sb=new StringBuffer();
				for(int i=0;i<19;i++){
					Object v=Parser01.parsevalue(data,pos,2,fraction);
					if(v!=null){
						sb.append(v.toString());
					}else{
						sb.append("null");
					}
					sb.append(",");
					pos+=2;
				}
				rt=sb.toString().substring(0,sb.length()-1);
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * 
	 * @param frame �ֽڴ������
	 * @param value ��������
	 * @param loc   ��ſ�ʼλ��
	 * @param len   �������
	 * @param fraction ���ݰ���С��λ��
	 * @return ʵ�ʱ��볤��
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			
			String[] vs=value.split(",");
			
			int pos=loc;
			for(int i=0;i<19;i++){								
				Parser01.constructor(frame,vs[i],pos,2,fraction);
				pos+=2;
			}			
		}catch(Exception e){
			//e.printStackTrace();
			throw new MessageEncodeException("invalid string:"+value);
		}
		return len;
	}
}
