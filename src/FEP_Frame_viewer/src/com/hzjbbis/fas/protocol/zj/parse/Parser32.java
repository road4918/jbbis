package com.hzjbbis.fas.protocol.zj.parse;

import java.text.NumberFormat;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * �����DI1DI0NN1NN2MMMM.MMRR.RRSS.SS
 * @author yangdinghuan
 *
 */
public class Parser32 {
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
				sb.append(String.valueOf(ParseTool.BytesToHexC(data,loc+9,2)));	//DI1DI0
				sb.append(",");
				sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc+8])));	//NN1
				sb.append(",");
				sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc+7])));	//NN2
				sb.append(",");
				sb.append(String.valueOf(((double)ParseTool.nBcdToDecimal(data,loc+4,3))/ParseTool.fraction[2]));	//MMMM.MM
				sb.append(",");
				sb.append(String.valueOf(((double)ParseTool.nBcdToDecimal(data,loc+2,2))/ParseTool.fraction[2]));	//RR.RR
				sb.append(",");
				sb.append(String.valueOf(((double)ParseTool.nBcdToDecimal(data,loc,2))/ParseTool.fraction[2]));	//SS.SS
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
				if(c=='.'){
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
				throw new MessageEncodeException("����� DI1DI0 NN1 NN2 MMMM.MM RR.RR SS.SS ��֡����:"+value);
			}
			
			String[] para=value.split(",");
			
			ParseTool.HexsToBytes(frame,loc+9,para[0]);	//DI
			frame[loc+8]=ParseTool.IntToBcd(Integer.parseInt(para[1]));
			frame[loc+7]=ParseTool.IntToBcd(Integer.parseInt(para[2]));
			double val=nf.parse(para[3]).doubleValue();
			val*=ParseTool.fraction[2];
			ParseTool.IntToBcd(frame,(int)val,loc+4,3);
			val=nf.parse(para[4]).doubleValue();
			val*=ParseTool.fraction[2];
			ParseTool.IntToBcd(frame,(int)val,loc+2,2);
			val=nf.parse(para[5]).doubleValue();
			val*=ParseTool.fraction[2];
			ParseTool.IntToBcd(frame,(int)val,loc,2);
		}catch(Exception e){
			throw new MessageEncodeException("����� DI1DI0 NN1 NN2 MMMM.MM RR.RR SS.SS ��֡����:"+value);
		}
		
		return len;
	}
}
