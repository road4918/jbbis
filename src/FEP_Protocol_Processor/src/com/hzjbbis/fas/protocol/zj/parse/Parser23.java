package com.hzjbbis.fas.protocol.zj.parse;

import java.text.NumberFormat;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ���ض�ֵʱ������HHmm NN XXXXXX.XX
 * @author yangdinghuan
 *
 */
public class Parser23 {
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
				sb.append(ParseTool.ByteToHex(data[loc+6]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+5]));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+4]));	//NN
				sb.append(",");
				int val=ParseTool.nBcdToDecimal(data,loc,4);	//XX
				sb.append(String.valueOf(((double)val)/ParseTool.fraction[2]));
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
				if(c==':'){
					continue;
				}
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� HH:mm NN XXXXXX.XX ��֡����:"+value);
			}
			
			String[] para=value.split(",");
			String[] time=para[0].split(":");
			
			frame[loc+6]=ParseTool.StringToBcd(time[0]);
			frame[loc+5]=ParseTool.StringToBcd(time[1]);			
			frame[loc+4]=ParseTool.StringToBcd(para[1]);
			
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			double val=nf.parse(para[2]).doubleValue();			
			val*=ParseTool.fraction[2];
			ParseTool.IntToBcd(frame,(int)val,loc,4);
		}catch(Exception e){
			throw new MessageEncodeException("����� HH:mm NN XXXXXX.XX ��֡����:"+value);
		}
		
		return len;
	}
}
