package com.hzjbbis.fas.protocol.zj.parse;

import java.text.NumberFormat;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ������λ��HEX����ֵ���������λ�Ƿ���λ��
 * @author yangdinghuan
 *
 */
public class Parser04 {
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
			ok=ParseTool.isAllFF(data,loc,len);
			if(!ok){
				boolean bn=((data[loc+len-1] & 0x80)>0);
				int val=ParseTool.nByteToIntS(data,loc,len);
				if(bn){
					val=-val;
				}
				if(fraction>0){
					NumberFormat snf=NumberFormat.getInstance();
					snf.setMinimumFractionDigits(fraction);
					snf.setMinimumIntegerDigits(1);
					snf.setGroupingUsed(false);
					rt=snf.format((double)val/ParseTool.fraction[fraction]);
				}else{
					rt=new Integer(val);
				}
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
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			String sn="";
			if(value.substring(0,1).equals("+")){
				sn=value.substring(1,value.length());
			}else{
				sn=value;
			}
			double val=nf.parse(sn).doubleValue();
			if(fraction>0){
				val*=ParseTool.fraction[fraction];
			}
			boolean bn=(val<0);
			if(bn){
				val=-val;
			}
			ParseTool.DecimalToBytes(frame,(int)val,loc,len);
			if(bn){
				frame[loc+len-1]=(byte)(frame[loc+len-1]  | 0x80);
			}
		}catch(Exception e){
			throw new MessageEncodeException("�����HEX����֡����:"+value);
		}
		return len;
	}
}
