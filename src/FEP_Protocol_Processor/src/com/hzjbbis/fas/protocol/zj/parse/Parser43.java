package com.hzjbbis.fas.protocol.zj.parse;

import java.util.Arrays;

import com.hzjbbis.exception.MessageEncodeException;


/**
 * ascii�ַ����������---0x00����䣬��������ں��ǰ����Щ�ն����0xAA��
 * @author netice
 *
 */
public class Parser43 {
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
			int begin=loc;
			for(int i=0;i<len;i++){	//remove prefix
				if(!(((data[loc+i] & 0xff)==0x00) || ((data[loc+i] & 0xff)>=0x80))){
					break;
				}
				begin++;
			}
			int rlen=0;
			for(int i=begin;i<loc+len;i++){	//remove suffix
				if(((data[i] & 0xff)==0x00) || ((data[i] & 0xff)>=0x80)){
					break;
				}
				rlen++;
			}
			if(rlen>0){
				byte[] apn=new byte[rlen];
				int iloc=begin+rlen-1;
				for(int i=0;i<rlen;i++){
					apn[i]=data[iloc];
					iloc--;
				}
				rt=new String(apn,"GBK");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return rt;
	}
	
	/**
	 * ��֡----����ԼĬ�ϣ����Ȳ���ʱ��λ���0x00
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=-1;
		try{
			Arrays.fill(frame,loc,loc+len-1,(byte)0x0);
			byte[] str=value.getBytes();
			int rlen=str.length;
			if(rlen>len){//�������ݹ���
				rlen=len;
			}
			if(fraction==0){
				int src=str.length-1;
				int dest=loc;
				for(int i=0;i<rlen;i++){
					frame[dest]=str[src];
					src--;
					dest++;
				}	
			}else{//��λ��0
				int src=0;
				int dest=loc+len-1;
				for(int i=0;i<rlen;i++){
					frame[dest]=str[src];
					src++;
					dest--;
				}	
			}
					
			slen=len;
		}catch(Exception e){
			throw new MessageEncodeException("����� ascii�ַ��� ��֡����:"+value);
		}
		return slen;
	}
}
