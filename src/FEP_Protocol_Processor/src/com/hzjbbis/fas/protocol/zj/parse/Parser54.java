package com.hzjbbis.fas.protocol.zj.parse;

import java.text.NumberFormat;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * �޹�����װ�õ�����:NNSSPP.PP1���� SSPP.PP16
 *
 */
public class Parser54 {
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
				sb.append(ParseTool.ByteToHex(data[loc+48]));
				sb.append(",");
				for (int i=15;i>=0;i--){
					sb.append(ParseTool.ByteToHex(data[loc+i*3+2]));
					sb.append(",");
					sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+i*3,2)/ParseTool.fraction[2]));
					sb.append(",");
				}				
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
	 * @param value ����ֵ(ǰ�����ж�����ַ�:NN,SS1,PP.PP1����SS16,PP.PP16)
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c==','){
					continue;
				}	
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� NN,SS1,PP.PP1����SS16,PP.PP16 ��֡����:"+value);
			}
			String[] para=value.split(",");			
			frame[loc+48]=ParseTool.StringToBcd(para[0]);		//NN
			for (int i=0;i<16;i++){
				double xx=nf.parse(para[32-i*2]).doubleValue()*ParseTool.fraction[2];			
				ParseTool.IntToBcd(frame,(int)xx,loc+i*3,2);	
				frame[loc+i*3+2]=ParseTool.IntToBcd(Integer.parseInt(para[32-i*2-1]));			
			}			
		}catch(Exception e){
			throw new MessageEncodeException("����� NN,SS1,PP.PP1����SS16,PP.PP16 ��֡����:"+value);
		}
		
		return len;
	}
}
