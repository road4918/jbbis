package com.hzjbbis.fas.protocol.zj.parse;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @filename	Parser39.java
 * @auther 		netice
 * @date		2006-4-5 16:21:59
 * @version		1.0
 * TODO
 */
public class Parser39 {
	private static final Log log=LogFactory.getLog(Parser39.class);
	
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
				int num=((data[1] & 0xff)<<8)+(data[0] & 0xff);
				int iloc=loc+2;
				rt=new ArrayList();
				for(int i=0;i<num;i++){
					/*BusRtu rtu=new BusRtu();
					rtu.setLogicAddress((String)Parser36.parsevalue(data,iloc,4,0));
					iloc+=4;
					rtu.setCommChannel(String.valueOf(data[iloc] & 0xff));
					iloc+=1;
					rtu.setCommAddress((String)Parser37.parsevalue(data,iloc,8,(data[iloc-1] & 0xff)));
					iloc+=8;
					rtu.setManufacturer(String.valueOf(data[iloc] & 0xff));
					iloc+=1;
					rtu.setPowerVoltage(new String(data,iloc,1,"GBK"));
					iloc+=1;
					rtu.setCustomerNo((String)Parser43.parsevalue(data,iloc,10,0));
					iloc+=10;
					rtu.setCustomerName((String)Parser43.parsevalue(data,iloc,20,0));
					iloc+=20;
					rtu.setStationNo((String)Parser43.parsevalue(data,iloc,12,0));
					iloc+=12;
					rtu.setPrincipalMobile((String)Parser43.parsevalue(data,iloc,14,0));
					iloc+=14;
					((List)rt).add(rtu);*/
//					if(i>0){
//						sb.append(",");
//					}
//					sb.append(Parser36.parsevalue(data,iloc,4,0));	//��ַ					
//					iloc+=4;
//					sb.append(",");
//					sb.append(String.valueOf(data[iloc] & 0xff));	//�ŵ�
//					iloc+=1;
//					sb.append(",");
//					sb.append(Parser37.parsevalue(data,iloc,8,(data[iloc-1] & 0xff)));	//ͨѶ��ַ
//					iloc+=8;
//					sb.append(",");
//					sb.append(String.valueOf(data[iloc] & 0xff));	//���̱��
//					iloc+=1;
//					sb.append(",");
//					sb.append(new String(data,iloc,1,"GBK"));	//��ѹ����
//					iloc+=1;
//					sb.append(",");
//					sb.append(Parser43.parsevalue(data,iloc,10,0));	//����
//					iloc+=10;
//					sb.append(",");
//					sb.append(Parser43.parsevalue(data,iloc,20,0));	//����
//					iloc+=20;
//					sb.append(",");
//					sb.append(Parser43.parsevalue(data,iloc,12,0));	//��
//					iloc+=12;
//					sb.append(",");
//					sb.append(Parser43.parsevalue(data,iloc,14,0));	//�ֻ�
//					iloc+=14;
				}
				rt=sb.toString();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return rt;
	}
	
	/**
	 * ��֡----����ԼĬ��
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ ���ŵ���ͨ�ŵ�ַ�����̱�ţ���ѹ���룬���ţ���������ƾֺţ��ֻ���
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=-1;
		try{
			String[] para=value.split(",");
			if((para!=null)&&(para.length>0)){
				int iloc=loc;				
				//Parser36.constructor(frame,para[0],iloc,4,0);
				ParseTool.RtuaToBytesC(frame,para[0],iloc,4);
				iloc+=4;
				
				if(!para[1].equals("null")){
					frame[iloc]=(byte)Integer.parseInt(para[1]);	//��ַ
				}else{
					frame[iloc]=(byte)0xff;
				}				
				iloc+=1;
				
				if(!para[2].equals("null")){
					if(fraction>0){//ǿ��ˢSIM����
						Parser37.constructor(frame,para[2],iloc,8,DataItemParser.COMM_TYPE_SMS);	//ͨ�ŵ�ַ
					}else{
						Parser37.constructor(frame,para[2],iloc,8,(frame[iloc-1] & 0xff));	//ͨ�ŵ�ַ
					}					
				}else{
					Arrays.fill(frame,iloc,iloc+8,(byte)0xff);
				}				
				iloc+=8;
				
				if(!para[3].equals("null")){
					frame[iloc]=(byte)Integer.parseInt(para[3]);	//���̱��
				}else{
					frame[iloc]=(byte)0xff;
				}				
				iloc+=1;
				
				if(!para[4].equals("null")){
					frame[iloc]=para[4].getBytes()[0];	//��ѹ����
				}else{
					frame[iloc]=(byte)0xff;
				}				
				iloc+=1;
				
				if(!para[5].equals("null")){
					Parser43.constructor(frame,para[5],iloc,10,0);	//����
				}else{
					Arrays.fill(frame,iloc,iloc+10,(byte)0xff);
				}				
				iloc+=10;
				
				if(!para[6].equals("null")){
					Parser43.constructor(frame,para[6],iloc,20,0);	//����
				}else{
					Arrays.fill(frame,iloc,iloc+20,(byte)0xff);
				}				
				iloc+=20;
				
				if(!para[7].equals("null")){
					Parser43.constructor(frame,para[7],iloc,12,0);	//��ƾֺ�
				}else{
					Arrays.fill(frame,iloc,iloc+12,(byte)0xff);
				}
				iloc+=12;
				
				if(!para[8].equals("null")){
					Parser43.constructor(frame,para[8],iloc,14,0);	//�ֻ�
				}else{
					Arrays.fill(frame,iloc,iloc+14,(byte)0xff);
				}
				
				slen=71;
			}
		}catch(Exception e){
			//throw new MessageEncodeException("����� �ն˲��� ��֡����:"+value);
			log.warn("����� �ն˲��� ��֡����:"+value);
		}
		return slen;
	}
}
