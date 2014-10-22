package com.hzjbbis.fas.protocol.zj.parse;

import com.hzjbbis.exception.MessageEncodeException;

/**
 * ��վͨѶ��ַ
 * @author yangdinghuan
 *
 */
public class Parser14 {
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
				int type=ParseTool.BCDToDecimal(data[loc+8]);
				String stype=ParseTool.ByteToHex(data[loc+8]);
				
				switch(type){
					case DataItemParser.COMM_TYPE_SMS:	//SMS					
						rt=stype+","+ParseTool.toPhoneCode(data,loc,8,0xAA);				
						break;
					case DataItemParser.COMM_TYPE_GPRS:					
						int port=ParseTool.nByteToInt(data,loc,2);	//net port					
						String ip=(data[loc+5] & 0xff)+"."+(data[loc+4] & 0xff)
							+"."+(data[loc+3] & 0xff)+"."+(data[loc+2] & 0xff);					
						rt=stype+","+ip+":"+port;
						break;
					case DataItemParser.COMM_TYPE_DTMF:					
						rt=stype+","+ParseTool.toPhoneCode(data,loc,8,0xAA);
						break;
					case DataItemParser.COMM_TYPE_ETHERNET:					
						port=ParseTool.nByteToInt(data,loc,2);	//net port					
						ip=(data[loc+5] & 0xff)+"."+(data[loc+4] & 0xff)
							+"."+(data[loc+3] & 0xff)+"."+(data[loc+2] & 0xff);
						rt=stype+","+ip+":"+port;
						break;
					case DataItemParser.COMM_TYPE_INFRA:					
						break;
					case DataItemParser.COMM_TYPE_RS232:					
						break;
					case DataItemParser.COMM_TYPE_CSD:					
						rt=stype+","+ParseTool.toPhoneCode(data,loc,8,0xAA);
						break;
					case DataItemParser.COMM_TYPE_RADIO:					
						break;
					default:
						break;
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
	 * @param value ����ֵ(ǰ�����ж�����ַ� nn,ip)
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			String[] para=value.split(",");
			int type=Integer.parseInt(para[0]);
			frame[loc+8]=(byte)type;
			switch(type){
				case DataItemParser.COMM_TYPE_SMS:
					//����
					ParseTool.StringToBcds(frame,loc,para[1]);
					int flen=((para[1].length()>>>1)+(para[1].length() & 0x01));
					for(int i=loc+flen;i<loc+len-1;i++){
						frame[i]=(byte)0xAA;
					}
					break;
				case DataItemParser.COMM_TYPE_GPRS:
					//IP
					ParseTool.IPToBytes(frame,loc,para[1]);
					frame[loc+6]=(byte)0xAA;
					frame[loc+7]=(byte)0xAA;
					break;
				case DataItemParser.COMM_TYPE_DTMF:
					//����
					ParseTool.StringToBcds(frame,loc,para[1]);
					flen=((para[1].length()>>>1)+(para[1].length() & 0x01));
					for(int i=loc+flen;i<loc+len-1;i++){
						frame[i]=(byte)0xAA;
					}
					break;
				case DataItemParser.COMM_TYPE_ETHERNET:
					//IP
					ParseTool.IPToBytes(frame,loc,para[1]);
					frame[loc+6]=(byte)0xAA;
					frame[loc+7]=(byte)0xAA;
					break;
				case DataItemParser.COMM_TYPE_INFRA:						
					break;
				case DataItemParser.COMM_TYPE_RS232:						
					break;
				case DataItemParser.COMM_TYPE_CSD:
					//����
					ParseTool.StringToBcds(frame,loc,para[1]);
					flen=((para[1].length()>>>1)+(para[1].length() & 0x01));
					for(int i=loc+flen;i<loc+len-1;i++){
						frame[i]=(byte)0xAA;
					}
					break;
				case DataItemParser.COMM_TYPE_RADIO:						
					break;
				default:
					break;
			}
		}catch(Exception e){
			throw new MessageEncodeException("����� ��վͨѶ��ַ ��֡����:"+value);
		}
		
		return len;
	}
}
