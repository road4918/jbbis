package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * BCD���ʾ��ʱ����ʼʱ�������һ���ֽڵĸ�ʽ1:hhmmNNMM
 *
 */
public class Parser53 {
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
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//hh
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//mm
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//NN	
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc]));	//MM		
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
	 * @param value ����ֵ(ǰ�����ж�����ַ� HH:MM,NN,MM)
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
				throw new MessageEncodeException("����� HH:mm NN MM��֡����:"+value);
			}
			String[] para=value.split(",");
			String[] time=para[0].split(":");
			
			frame[loc]=ParseTool.StringToBcd(para[2]);//MM
			frame[loc+1]=ParseTool.StringToBcd(para[1]);//NN
			frame[loc+3]=ParseTool.StringToBcd(time[0]);//hh
			frame[loc+2]=ParseTool.StringToBcd(time[1]);//mm
		}catch(Exception e){
			throw new MessageEncodeException("����� hh:mm NN MM��֡����:"+value);
		}
		
		return len;
	}
}
