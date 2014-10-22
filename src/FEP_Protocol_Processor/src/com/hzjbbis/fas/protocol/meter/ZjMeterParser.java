package com.hzjbbis.fas.protocol.meter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.data.DataItem;
import com.hzjbbis.fas.protocol.meter.conf.MeterProtocolDataItem;
import com.hzjbbis.fas.protocol.meter.conf.MeterProtocolDataSet;
import com.hzjbbis.fas.protocol.zj.parse.DataItemParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


/**
 * �㽭���Լ������
 * @author netice
 * 			�㽭���Լ
 * 		0X68-------------֡ͷ��ʶ1
 * 		L   -------------���ݳ����ӱ��ַ��ʼ��У���֮ǰ�������ֽڳ��ȣ�
 * 		L   -------------���ݳ����ӱ��ַ��ʼ��У���֮ǰ�������ֽڳ��ȣ�
 * 		0X68-------------֡ͷ��ʶ2
 * 		addr-------------���ַ
 * 		CMDL-------------�����ֵ��ֽ�
 * 		CMDH-------------�����ָ��ֽ�
 * 		Data-------------������
 * 		CS  -------------У��ͣ����ݳ���Ӧ�ֽڵĺ�ģ256��
 * 		0X0D-------------֡β��ʶ
 */
public class ZjMeterParser implements IMeterParser{
	private final Log log=LogFactory.getLog(ZjMeterParser.class);
	private MeterProtocolDataSet dataset;
	
	public ZjMeterParser(){
		try{
			dataset=MeterProtocolFactory.createMeterProtocolDataSet("ZJMeter");
		}catch(Exception e){
			log.error("�㽭���Լ��ʼ��ʧ��");
		}
	}
	
	public String[] convertDataKey(String[] datakey) {
		String[] rt=null;
		try{
			if((datakey!=null)&&(datakey.length>0)){
				List parents=getParents(dataset);
				if(parents!=null){
					rt=new String[datakey.length];
					for(int i=0;i<datakey.length;i++){
						String dkprefix=datakey[i].substring(0,3);
						for(Iterator iter=parents.iterator();iter.hasNext();){
							MeterProtocolDataItem di=(MeterProtocolDataItem)iter.next();
							boolean bfind=false;
							if(di.getZjcode()!=null && di.getZjcode().length()>0){
								//���⴦��B6FF
								if(di.getZjcode().equalsIgnoreCase("B6FF")){
									bfind=dkprefix.substring(0,2).equalsIgnoreCase("B6");
								}else{
									bfind=di.getZjcode().substring(0,3).equals(dkprefix);
								}								
							}							
							if((!bfind) && di.getZjcode2()!=null && di.getZjcode2().length()>0){
								bfind=di.getZjcode2().substring(0,3).equals(dkprefix);
							}
							if(bfind){//�ҵ�������
								boolean block=false;
								if(di.getZjcode()!=null){
									block=di.getZjcode().equals(datakey[i])
											||datakey[i].equalsIgnoreCase("B61F")
											||datakey[i].equalsIgnoreCase("B62F");									
								}
								if((!block) && di.getZjcode2()!=null){
									block=di.getZjcode2().equals(datakey[i]);
								}
								if(block){//�ٲ����ݿ�
									rt[i]=di.getCode();
								}else{
									List cdks=di.getChildarray();
									for(Iterator iter1=cdks.iterator();iter1.hasNext();){
										MeterProtocolDataItem cdi=(MeterProtocolDataItem)iter1.next();										
										if(cdi.getZjcode()!=null && cdi.getZjcode().equals(datakey[i])){
											rt[i]=cdi.getCode();
											break;
										}
										if(cdi.getZjcode2()!=null && cdi.getZjcode2().equals(datakey[i])){
											rt[i]=cdi.getCode();
											break;
										}
									}
								}
								break;
							}
						}
						if((rt[i]==null) || rt[i].equals("")){
							log.info("��֧�ֵ������ٲ⣺"+datakey[i]);
							rt=null;
							break;
						}
					}
				}else{
					log.info("�յı��Լ���ϣ�������Լ����");
				}
			}
		}catch(Exception e){
			log.error("convert datakey",e);
		}		
		return fixCode(rt);
	}
	
	/**
	 * �������ݱ��룬ͬһ���ݿ������������ݿ�����ٲ�
	 * @param codes
	 * @return
	 */
	private String[] fixCode(String[] codes){
		String[] rt=null;
		try{			
			rt=new String[codes.length];
			rt[0]=codes[0];
			int j=1;
			for(int i=1;i<codes.length;i++){
				boolean fixed=false;
				for(int k=0;k<j;k++){
					if(rt[k].equalsIgnoreCase(codes[i])){	//�ظ��ı���
						fixed=true;
						break;
					}else{
						if(rt[k].substring(0, 3).equalsIgnoreCase(codes[i].substring(0, 3))){
							fixed=true;
							rt[k]=rt[k].substring(0, 3)+"0";
						}
					}					
				}
				if(!fixed){	//δ������ǰ������ݱ�����
					rt[j]=codes[i];
					j++;
				}
			}
		}catch(Exception e){
			//
		}
		return rt;
	}
	
	private List getParents(MeterProtocolDataSet dataset){
		List rt=null;
		try{
			Hashtable dks=dataset.getDataset();
			Enumeration dkey=dks.elements();
			rt=new ArrayList();
			while(dkey.hasMoreElements()){
				MeterProtocolDataItem di=(MeterProtocolDataItem)dkey.nextElement();
				if(di.getChildarray()==null){
					continue;
				}
				if(di.getChildarray().size()<=0){
					continue;
				}
				rt.add(di);
			}
		}catch(Exception e){
			log.error("pretreatment protocol",e);
		}
		return rt;
	}
	
	/**
	 * ˵�����㽭���Լ�Ƚ����⣬�����ٲ������ݱ�ʶ�����Ҫ�ٲ���������
	 * 		ֻ���ҳ����������ĸ���ʶ�����ݿ��ʶ�����ٲ�
	 */
	public byte[] constructor(String[] datakey,DataItem para) {
		String dk="";
		byte[] frame=null;
		try{
			if((datakey!=null)&&(datakey.length>0)
				&&(para!=null)&&(para.getProperty("point")!=null)){	//check para				
				
				if(datakey.length==1){	//just one datakey
					dk=datakey[0];
				}else{
					dk=datakey[0].substring(0,3)+"0";
					for(int i=1;i<datakey.length;i++){
						if(dk.substring(0,2).equals(datakey[i].substring(0,2))){//0XXX��0Xһ��Ҫ��ͬ
							if(!dk.substring(2,3).equals(datakey[i].substring(2,3))){
								if(!dk.substring(2,3).equals("0")){
									dk=dk.substring(0,2)+"00";
								}
							}
						}else{
							//error���������ٲ�������(�㽭������ݱ�ʶΪXXX���Ұ�������Ϊ0XXX)
							log.info("Ŀǰֻ֧���ٲ�ͬ�����ݣ���ͬ��������ֱ��ٲ⣡");
							dk="";
							break;
						}
					}
				}
				if(dk.length()>0){
					frame=new byte[9];
					constructFrameCallData(frame,dk,ParseTool.HexToByte((String)para.getProperty("point")));
				}				
			}			
		}catch(Exception e){
			log.error("Construct ZJ meter frame ",e);
		}
		return frame;
	}
	
	private void constructFrameCallData(byte[] frame,String datakey,byte maddr){
		frame[0]=0x68;
		frame[1]=0x3;
		frame[2]=0x3;
		frame[3]=0x68;
		frame[4]=maddr;
		ParseTool.HexsToBytes(frame,5,datakey);
		frame[7]=ParseTool.calculateCS(frame,4,3);
		frame[8]=0x0D;
	}
	
	/**
	 * ������֡
	 */
	public Object[] parser(byte[] data,int loc,int len) {
		List result=null;
		try{
			ZjMeterFrame frame=new ZjMeterFrame();
			frame.parse(data,loc,len);
			if(frame.getDatalen()>0){	//�����а�����֡
				result=new ArrayList();
				//��ȡ��֡����
				int datalen=frame.getDatalen();
				if(datalen==1){	//���п�����֡
					//������
				}else{
					byte[] framedata=frame.getData();
					String meteraddr=frame.getMeteraddr();	//���ַ
					DataItem item=new DataItem();
					item.addProperty("value",meteraddr);
					item.addProperty("datakey","8902");
					result.add(item);
					if(datalen==2){	//����Ӧ��֡
						int rtype=(framedata[frame.getPos()+1] & 0xff);
						if(rtype==ZjMeterFrame.FLAG_REPLY_ERROR){
							//��Ӧ�����֡
						}
						if(rtype==ZjMeterFrame.FLAG_REPLY_OK){
							//��Ӧ��ȷ��֡
						}						
					}else{	//����֡
						int iloc=frame.getPos();
						iloc++;
						while(iloc<(framedata.length-2)){
							String datakey=ParseTool.BytesToHexC(framedata,iloc,2);	//���ݱ�ʶ
							MeterProtocolDataItem mpd=dataset.getDataItem(datakey);
							iloc+=2;	//�������ݿ�ʼλ��
							if(mpd!=null){
								List children=mpd.getChildarray();
								if((children!=null)&& (children.size()>0)){	//������
									for(int ic=0;ic<children.size();ic++){
										MeterProtocolDataItem cmpd=(MeterProtocolDataItem)children.get(ic);
										if((framedata[iloc] & 0xff)==ZjMeterFrame.FLAG_BLOCK_DATA){	//�����ݽ�����־											
											break;
										}
										if((framedata[iloc] & 0xff)==ZjMeterFrame.FLAG_NO_DATA){	//����ȱʧ��־
											iloc++;
											continue;
										}
										//����������										
										Object val=parseItem(framedata,iloc,cmpd);
										toZjDataItem(val,cmpd,result);
										iloc+=cmpd.getLength();
									}
									if((framedata[iloc] & 0xff)==ZjMeterFrame.FLAG_BLOCK_DATA){	//�����ݽ�����־											
										iloc++;
									}
								}else{	//������
									if((framedata[iloc] & 0xff)==ZjMeterFrame.FLAG_NO_DATA){	//����ȱʧ��־
										iloc++;										
									}else{										
										Object val=parseItem(framedata,iloc,mpd);
										toZjDataItem(val,mpd,result);
										iloc+=mpd.getLength();
									}									
								}
							}else{
								//�������ݱ�ʶ
								break;
							}
						}
					}
				}				
			}
		}catch(Exception e){
			log.error("�����㽭���Լ",e);
		}
		if(result!=null){
			return result.toArray();
		}
		return null;
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
	
	private void toZjDataItem(Object val,MeterProtocolDataItem mpd,List result){
		try{
			if(mpd.getZjcode2()!=null && mpd.getZjcode2().length()>0){//��Ӧ����㽭��Լ����
				String[] vals=((String)val).split(",");
				DataItem item=new DataItem();
				item.addProperty("value",vals[0]);
				item.addProperty("datakey",mpd.getZjcode());
				result.add(item);
				if(vals.length>1){
					DataItem item2=new DataItem();
					item2.addProperty("value",vals[1]);
					item2.addProperty("datakey",mpd.getZjcode2());
					result.add(item2);
				}
			}else{
				DataItem item=new DataItem();
				item.addProperty("value",val);
				item.addProperty("datakey",mpd.getZjcode());
				result.add(item);
			}
		}catch(Exception e){
			log.error("convert to zj data",e);
		}
	}
}
