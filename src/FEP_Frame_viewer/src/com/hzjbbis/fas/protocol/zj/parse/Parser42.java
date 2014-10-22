package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * BCD���ʾ��ʱ����ʼʱ��ͷ��ʺ�NNhhmm
 * @author netice
 *
 */
public class Parser42 {
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
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//hh
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));	//mm
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
	 * @param value ����ֵ(ǰ�����ж�����ַ� NN,HH:MM)
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
				throw new MessageEncodeException("����� NN hh:mm ��֡����:"+value);
			}
			//construct
			String[] para=value.split(",");
			String[] time=para[1].split(":");
			
			frame[loc+2]=ParseTool.StringToBcd(para[0]);	//NN
			frame[loc+1]=ParseTool.StringToBcd(time[0]);	//hh
			frame[loc]=ParseTool.StringToBcd(time[1]);	//mm
		}catch(Exception e){
			throw new MessageEncodeException("����� NN hh:mm ��֡����:"+value);
		}
		
		return len;
	}
}
