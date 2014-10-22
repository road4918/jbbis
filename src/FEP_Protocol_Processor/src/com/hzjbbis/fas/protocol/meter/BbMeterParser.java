package com.hzjbbis.fas.protocol.meter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.data.DataItem;
import com.hzjbbis.fas.protocol.meter.conf.MeterProtocolDataItem;
import com.hzjbbis.fas.protocol.meter.conf.MeterProtocolDataSet;
import com.hzjbbis.fas.protocol.zj.parse.DataItemParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * @filename	BbMeterParser.java
 * @auther 		netice
 * @date		2006-6-21 15:40:55
 * @version		1.0
 * TODO			������Լ
 * 				0x68---------------------֡ͷ��ʶ1
 * 				A0-----------------------��ַ����λ��
 * 				A1-----------------------��ַ
 * 				A2-----------------------��ַ
 * 				A3-----------------------��ַ
 * 				A4-----------------------��ַ
 * 				A5-----------------------��ַ����λ��
 * 				0x68---------------------֡ͷ��ʶ2
 * 				c------------------------������ bit7�����䷽��0-���� 1-Ӧ�� bit6���쳣��ʶ 0-���� 1-�쳣 
 * 											   bit5������֡��ʶ 0-��֡ 1-�к���֡
 *                                             bit0-bit4 ������
 *                                             00000 ��  ����
 *				                               00001 ��  ������
 *				                               00010 ��  ����������
 *				                               00011 ��  �ض�����
 *				                               00100 ��  д����
 *				                               01000 ��  �㲥Уʱ
 *				                               01010 ��  д�豸��ַ
 *				                               01100 ��  ����ͨѶ����
 *				                               01111 :   �޸�����
 *				                               10000 :   �����������
 *				L-------------------------�����򳤶�
 *				DATA----------------------������ �����ݲ�����200��д���ݲ�����50
 *				CS------------------------У�� ��֡ͷ��ʶ1��CSǰ�����ֽڵĺ�ģ256
 *				0x16----------------------֡β
 */
public class BbMeterParser implements IMeterParser{
	private final Log log=LogFactory.getLog(BbMeterParser.class);
	private MeterProtocolDataSet dataset;
	
	public BbMeterParser(){
		try{
			dataset=MeterProtocolFactory.createMeterProtocolDataSet("BBMeter");
		}catch(Exception e){
			log.error("������Լ��ʼ��ʧ��");
		}
	}
	
	/**
	 * ˵����������Լ�����ݱ�ʶ���㽭���Լ�����ݱ�ʶһ�£��㽭���Լ�е����ݼ���
	 *      ������Լ���ݼ���һ���Ӽ�
	 *      ���Ա�������ת�����ܼ�������Ҫ�Ǽ������ݱ�ʶ�ظ�
	 */
	public String[] convertDataKey(String[] datakey) {
		String[] rt=null;
		try{
			if(datakey!=null && datakey.length>0){//�����ݱ�ʶҪת��
				rt=new String[datakey.length];
				for(int i=0;i<datakey.length;i++){
					if((datakey[i]!=null) && datakey[i].equalsIgnoreCase("8902")){
						addDataKey(rt,"C034");
					}else{
						addDataKey(rt,datakey[i]);
					}
				}
			}
		}catch(Exception e){
			log.error("��������ݱ�ʶת��",e);
		}
		return rt;
	}
	
	/**
	 * ����δ�������ݵ�key������
	 * @param datakeys
	 * @param dkey
	 * ˵�������ݱ�ʶΪXXXX�����֧���ٲ�XXFF���ݿ飬��֧��FFFF��XFFF����Ϊ����̫��
	 */
	private void addDataKey(String[] datakeys,String dkey){
		for(int i=0;i<datakeys.length;i++){
			if(datakeys[i]==null || datakeys[i].equals("")){//������δ����
				if(dkey.substring(0,1).equalsIgnoreCase("F") || dkey.substring(1,2).equalsIgnoreCase("F")){
					//���������ٲ�FFFF��XFFF
					break;
				}
				datakeys[i]=dkey;
				break;
			}
			String char1=datakeys[i].substring(0,1);
			String char2=datakeys[i].substring(1,2);
			String char3=datakeys[i].substring(2,3);
			if(char1.equalsIgnoreCase(dkey.substring(0,1))
				&& char2.equalsIgnoreCase(dkey.substring(1,2))
				&& char3.equalsIgnoreCase(dkey.substring(2,3))){//��һ�����ݿ�������
				
				StringBuffer sb=new StringBuffer();
				sb.append(char1);
				sb.append(char2);
				//С���ݿ�
				sb.append(char3);
				sb.append("F");
				
				datakeys[i]=sb.toString();
				sb=null;
				break;
			}
		}
	}
	
	/**
	 * datakeyֻ���ܵ�һ�����ݱ�ʶ����֧��������֡
	 */
	public byte[] constructor(String[] datakey, DataItem para) {
		byte[] frame=null;
		try{
			if((datakey!=null)&&(datakey.length>0)
					&&(para!=null)&&(para.getProperty("point")!=null)){	//check para
				frame=new byte[14];
				String maddr=(String)para.getProperty("point");//���ַ
				String dkey=datakey[0];
				frame[0]=0x68;
				ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
				frame[7]=0x68;
				frame[8]=0x01;
				frame[9]=0x02;
				ParseTool.HexsToBytes(frame,10,dkey);
				frame[10]=(byte)(frame[10]+0x33);
				frame[11]=(byte)(frame[11]+0x33);
				frame[12]=ParseTool.calculateCS(frame,0,12);	//cs
				frame[13]=0x16;	//cs
			}
		}catch(Exception e){
			
		}
		return frame;
	}

	public Object[] parser(byte[] data, int loc, int len) {
		List result=null;
		try{
			BbMeterFrame frame=new BbMeterFrame();
			frame.parse(data,loc,len);
			if(frame.getDatalen()>0){	//�����а�����֡��֡�а�������
				result=new ArrayList();
				//��ȡ��֡����
				int datalen=frame.getDatalen();
				String meteraddr=frame.getMeteraddr();	//���ַ
				DataItem ma=new DataItem();
				ma.addProperty("value",meteraddr);
				ma.addProperty("datakey","8902");
				result.add(ma);
				
				int ctrl=frame.getCtrl();	/*������*/
				if((ctrl & 0x40)<=0){//����Ӧ��
					byte[] framedata=frame.getData();
					
					int pos=frame.getPos();
					switch(ctrl & 0x1F){
						case 1:	//������
							String datakey=ParseTool.BytesToHexC(framedata,pos,2);
							MeterProtocolDataItem item=dataset.getDataItem(datakey);
							pos+=2;
							if(item!=null){//֧�ֵ����ݱ�ʶ
								parseValues(framedata,pos,item,result);
							}
							break;
						default:
							break;
					}
				}else{
					//�쳣Ӧ��
					
				}
			}
		}catch(Exception e){
			log.error("������Լ",e);
		}
		if(result!=null){
			return result.toArray();
		}
		return null;
	}
	
	/**
	 * ����������
	 * @param data		��֡
	 * @param pos		��ǰ������ʼλ��
	 * @param item		��ǰ������������
	 * @param results	�����
	 */
	private int parseValues(byte[] data,int pos,MeterProtocolDataItem item,List results){
		int rt=0;
		try{
			int loc=pos;
			if(item.getChildarray()!=null && item.getChildarray().size()>0){
				List children=item.getChildarray();
				for(int i=0;i<children.size();i++){
					if((data[loc] & 0xFF)==BbMeterFrame.FLAG_BLOCK_DATA){//���ݿ����
						rt+=1;
						break;
					}
					if(loc>=data.length){//�����ݿ��Խ�����Ҳû�������������������Ӧ���Ǵ��������Ⱥ���
						break;
					}
					int vlen=parseValues(data,loc,(MeterProtocolDataItem)children.get(i),results);
					if(vlen<=0){
						rt=0;
						break;
					}
					loc+=vlen;
					rt+=vlen;
				}
			}else{
				DataItem di=new DataItem();
				di.addProperty("datakey",item.getZjcode());
				Object val=parseItem(data,pos,item);
				di.addProperty("value",val);
				results.add(di);
				rt=item.getLength();
			}
		}catch(Exception e){
			rt=0;
			log.error("�������������",e);
		}
		return rt;
	}
	
	/**
	 * ������������
	 * @param frame
	 * @param loc
	 * @param mpd
	 * @return
	 */
	private Object parseItem(byte[] frame,int loc,MeterProtocolDataItem mpd){
		Object val=DataItemParser.parsevalue(frame,loc,mpd.getLength(),mpd.getFraction(),mpd.getType());
		return val;
	}
}
