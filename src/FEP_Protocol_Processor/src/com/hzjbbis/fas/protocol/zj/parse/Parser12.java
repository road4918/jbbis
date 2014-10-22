package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * BCD���ʾ��ʱ����ʼʱ��ͷ��ʺ�hhmmNN
 * @author yangdinghuan
 *
 */
public class Parser12 {
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
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//hh
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//mm
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc]));	//NN			
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
	 * @param value ����ֵ(ǰ�����ж�����ַ� HH:MM,NN)
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
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� HH:mm NN ��֡����:"+value);
			}
			String[] para=value.split(",");
			String[] time=para[0].split(":");
			
			frame[loc]=ParseTool.StringToBcd(para[1]);
			frame[loc+2]=ParseTool.StringToBcd(time[0]);
			frame[loc+1]=ParseTool.StringToBcd(time[1]);
		}catch(Exception e){
			throw new MessageEncodeException("����� hh:mm NN ��֡����:"+value);
		}
		
		return len;
	}
}
