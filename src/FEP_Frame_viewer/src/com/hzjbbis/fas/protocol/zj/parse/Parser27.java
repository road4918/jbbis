package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * �м�����
 * @author yangdinghuan
 *
 */
public class Parser27 {
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
				sb.append(ParseTool.ByteToHex(data[loc]));	//TT
				sb.append(",");
				sb.append(String.valueOf(data[loc+2] & 0xff)); //TS--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//TS--UU
				sb.append(",");
				sb.append(String.valueOf(data[loc+4] & 0xff)); //TI--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//TI--UU
				sb.append(",");
				sb.append(String.valueOf(data[loc+6] & 0xff)); //RS--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+5]));	//RS--UU
				sb.append(",");
				sb.append(String.valueOf(data[loc+8] & 0xff)); //RI--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+7]));	//RI--UU
				sb.append(",");
				sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc+9]))); //RDI
				sb.append(",");
				sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc+10]))); //PN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+11]));	//PS
				sb.append(",");
				sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,loc+12,2)));	//SP
				sb.append(",");
				sb.append(String.valueOf(data[loc+14] & 0xff)); //WT
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+15]));	//CC
				sb.append(",");
				sb.append(String.valueOf(ParseTool.nByteToInt(data,loc+16,2)));	//GF
				sb.append(",");
				sb.append(String.valueOf(ParseTool.nByteToInt(data,loc+18,2)));	//GL
				sb.append(",");
				int cl=ParseTool.BCDToDecimal(data[loc+20]);
				sb.append(String.valueOf(cl)); //CL	
				sb.append(",");
				if(cl<=32){	//CI ������32
					sb.append(ParseTool.BytesToHexL(data,loc+21,cl));
				}
				rt= sb.toString();
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
		int nums=0;
		int slen=-1;
		
		try{
			String[] para=value.split(",");
			frame[loc]=ParseTool.IntToBcd(Integer.parseInt(para[0]));	//TT
			
			frame[loc+1]=ParseTool.StringToBcd(para[2]);	//TS--UU			
			frame[loc+2]=(byte)(Integer.parseInt(para[1]) & 0xff);	//NN
			
			frame[loc+3]=ParseTool.StringToBcd(para[4]);	//TI--UU			
			frame[loc+4]=(byte)(Integer.parseInt(para[3]) & 0xff);	//NN
			
			frame[loc+5]=ParseTool.StringToBcd(para[6]);	//RS--UU
			frame[loc+6]=(byte)(Integer.parseInt(para[5]) & 0xff);	//NN
			
			frame[loc+7]=ParseTool.StringToBcd(para[8]);	//RI--UU
			frame[loc+8]=(byte)(Integer.parseInt(para[7]) & 0xff);	//NN
			
			frame[loc+9]=ParseTool.IntToBcd(Integer.parseInt(para[9]));	//RDI
			
			frame[loc+10]=ParseTool.IntToBcd(Integer.parseInt(para[10]));	//PN
			
			frame[loc+11]=(byte)(Integer.parseInt(para[11]));	//PS
			
			nums=Integer.parseInt(para[12]);
			ParseTool.IntToBcd(frame,nums,loc+12,2);	//SP
			
			frame[loc+14]=ParseTool.IntToBcd(Integer.parseInt(para[13]));	//WT
			
			frame[loc+15]=ParseTool.HexToByte(para[14]);	//CC
			
			ParseTool.DecimalToBytes(frame,Integer.parseInt(para[15]),loc+16,2);	//CF
			ParseTool.DecimalToBytes(frame,Integer.parseInt(para[16]),loc+18,2);	//CF
			
			nums=Integer.parseInt(para[17]);
			frame[loc+20]=ParseTool.IntToBcd(nums);	//din
			
			if(nums!=(para[18].length()/2)){
				//wrong para
				System.out.println("task para is error");
			}			
			ParseTool.HexsToBytesCB(frame,loc+21,para[18]);
			slen=nums+21;
		}catch(Exception e){
			throw new MessageEncodeException("����� �м����� ��֡����:"+value);
		}
		
		return slen;
	}
}
