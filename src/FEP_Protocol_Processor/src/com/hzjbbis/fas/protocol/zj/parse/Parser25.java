package com.hzjbbis.fas.protocol.zj.parse;

import java.text.NumberFormat;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ���ι�����SNNNNNNNXX
 * @author yangdinghuan
 *
 */
public class Parser25 {
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
				boolean bn=((data[loc+len-1] & 0x10)>0);
				int val=ParseTool.nBcdToDecimalS(data,loc+1,4);
				if(bn){
					val=-val;
				}
				sb.append(String.valueOf(val));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc]));
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
	 * @param value ����ֵ
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
				if(c=='-'){
					continue;
				}
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� SNNNNNNN XX ��֡����:"+value);
			}
			String[] para=value.split(",");
			
			int val=nf.parse(para[0]).intValue();			
			boolean bn=(val<0);
			if(bn){
				val=-val;
			}
			ParseTool.IntToBcd(frame,val,loc+1,4);
			if(bn){
				frame[loc+4]=(byte)((frame[loc+4] & 0xf) | 0x10);
			}
			
			frame[loc]=ParseTool.StringToBcd(para[1]);
		}catch(Exception e){
			throw new MessageEncodeException("����� SNNNNNNN XX ��֡����:"+value);
		}
		
		return len;
	}
}
