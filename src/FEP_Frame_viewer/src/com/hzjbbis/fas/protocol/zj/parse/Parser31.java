package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ����������MMDI1DI0cc1NN1cc2NN2����cc8NN8
 * @author yangdinghuan
 *
 */
public class Parser31 {
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
				sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc+18])));	//MM
				sb.append(",");
				sb.append(String.valueOf(ParseTool.BytesToHexC(data,loc+16,2)));	//DI1DI0		
				int iloc=loc+14;
				for(int i=0;i<8;i++){
					sb.append(",");
					sb.append(ParseTool.ByteToHex(data[iloc+1]));	//cc
					sb.append(",");
					sb.append(String.valueOf(ParseTool.BCDToDecimal(data[iloc]))); //NN
					iloc-=2;
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
	 * @param value ����ֵ
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
				if(c>='0' && c<='9'){
					continue;
				}
				if(c>='A' && c<='F'){
					continue;
				}
				if(c>='a' && c<='f'){
					continue;
				}
				throw new MessageEncodeException("����� MM DI1DI0 cc1NN1cc2NN2����cc8NN8 ��֡����:"+value);
			}
			
			String[] para=value.split(",");
			frame[loc+18]=ParseTool.IntToBcd(Integer.parseInt(para[0]));	//MM
			ParseTool.HexsToBytes(frame,loc+16,para[1]);	//DI
			int iloc=14;
			int ipara=2;
			for(int i=0;i<8;i++){
				frame[iloc+1]=ParseTool.IntToBcd(Integer.parseInt(para[ipara]));	//cc
				frame[iloc]=ParseTool.IntToBcd(Integer.parseInt(para[ipara+1]));	//NN
				iloc-=2;
				ipara+=2;
			}			
		}catch(Exception e){
			throw new MessageEncodeException("����� MM DI1DI0 cc1NN1cc2NN2����cc8NN8 ��֡����:"+value);
		}
		
		return len;
	}
}
