package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * BCD���ʾ��ʱ�����ֵ YYMMDDHHmm�͸�ʽ2
 *
 */
public class Parser52 {
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
			ok=ParseTool.isHaveValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append("20");
				sb.append(ParseTool.ByteToHex(data[loc+len-1])); 	//YY
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+len-2]));	//MM
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+len-3]));	//DD
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+len-4]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+len-5]));	//mm
				sb.append(",");
				boolean bn=((data[loc+len-5-1] & 0x10)>0);
				int val=ParseTool.nBcdToDecimalS(data,loc,len-5);
				if(bn){
					val=-val;
				}			
				sb.append(String.valueOf((double)val/ParseTool.fraction[fraction]));	//xx
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
	 * @param value ����ֵ(ǰ�����ж�����ַ� YYYY-MM-DD HH:MM,��ʽ2)
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
				if(c=='-'){
					continue;
				}
				if(c=='.'){
					continue;
				}
				if(c==' '){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� YYYY-MM-DD HH:mm ��ʽ2 ��֡����:"+value);
			}
			String[] para=value.split(",");
			String[] dpara=para[0].split(" ");
			String[] date=dpara[0].split("-");
			String[] time=dpara[1].split(":");
			
			Parser02.constructor(frame,para[1],loc,len-5,fraction);
			frame[loc+len-1]=ParseTool.StringToBcd(date[0]);
			frame[loc+len-2]=ParseTool.StringToBcd(date[1]);
			frame[loc+len-3]=ParseTool.StringToBcd(date[2]);
			frame[loc+len-4]=ParseTool.StringToBcd(time[0]);
			frame[loc+len-5]=ParseTool.StringToBcd(time[1]);
		}catch(Exception e){
			throw new MessageEncodeException("����� YYYY-MM-DD HH:mm ��ʽ2 ��֡����:"+value);
		}
		
		return len;
	}
}
