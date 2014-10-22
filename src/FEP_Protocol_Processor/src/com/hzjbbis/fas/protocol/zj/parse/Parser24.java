package com.hzjbbis.fas.protocol.zj.parse;

import java.util.Arrays;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ����ִ��ʱ������MSDSMEDETIN3N2N1N0
 * @author yangdinghuan
 *
 */
public class Parser24 {
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
			ok=ParseTool.isValidBCD(data,loc+4,len-4);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+8]));	//MS
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+7]));	//DS
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+6]));	//ME
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+5]));	//DE
				sb.append(",");
				int ti=data[loc+4] & 0xff;
				sb.append(ParseTool.ByteToHex(data[loc+4]));	//TI
				sb.append(",");
				switch(ti){
					case 0x04:	//��
						sb.append(ParseTool.ByteToHex(data[loc]));	//N0
						break;
					case 0x05:	//��
//						for(int i=0;i<4;i++){
//							int flag=ParseTool.ByteToFlag(data[loc+i]);
//							if(flag>0){
//								sb.append(String.valueOf((i<<3)+flag));	//N0
//								break;
//							}
//						}
						sb.append(ParseTool.BytesBitC(data,loc,4));
						break;
					case 0x06:	//��
						sb.append(ParseTool.ByteBitC(data[loc]));	//N0 bit0������
						break;
					default:
						sb.append("0");
						break;
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
				if(c=='-'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� MS-DS ME-DE TI N3N2N1N0 ��֡����:"+value);
			}
			
			String[] para=value.split(",");
			String[] sdate=para[0].split("-");
			String[] edate=para[1].split("-");
			
			frame[loc+8]=ParseTool.StringToBcd(sdate[0]);	//MS
			frame[loc+7]=ParseTool.StringToBcd(sdate[1]);
			frame[loc+6]=ParseTool.StringToBcd(edate[0]);	//ME
			frame[loc+5]=ParseTool.StringToBcd(edate[1]);
			
			int ti=Integer.parseInt(para[2]);
			frame[loc+4]=(byte)(ti % 10);	//δ���Ϸ���
			Arrays.fill(frame,loc,loc+3,(byte)0x0);
			switch(ti){
			case 0x04:	//��
				frame[loc]=ParseTool.IntToBcd(Integer.parseInt(para[3]));
				break;
			case 0x05:	//��
				ParseTool.bitToBytesC(frame,para[3],loc);
				break;
			case 0x06:	//��
				ParseTool.bitToBytesC(frame,para[3],loc);	//�Ժ�Ҫ�ÿɶ���Ϣ����֮������һ �����
				break;
			default:
				break;
		}			
		}catch(Exception e){
			throw new MessageEncodeException("����� MS-DS ME-DE TI N3N2N1N0 ��֡����:"+value);
		}
		
		return len;
	}
}
