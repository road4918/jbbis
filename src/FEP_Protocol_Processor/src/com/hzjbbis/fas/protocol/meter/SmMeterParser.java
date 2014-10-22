package com.hzjbbis.fas.protocol.meter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.data.DataItem;


/**
 *@filename	SmMeterParser.java
 *@auther	netice
 *@date		2007-3-4
 *@version	1.0
 *TODO		�����ӱ��
 */
public class SmMeterParser implements IMeterParser{
	private final Log log=LogFactory.getLog(SmMeterParser.class);
	
	public byte[] constructor(String[] datakey, DataItem para) {		
		//�����ӱ��һ���ٻ���������
		return new byte[]{0x2F,0x3F,0x21,0x0D,0x0A};
	}

	public String[] convertDataKey(String[] datakey) {		
		//�����ӱ���ݲ���Ҫת��
		return datakey;
	}

	public Object[] parser(byte[] data, int loc, int len) {
		List result=null;
		
		try{
			SmMeterFrame frame=new SmMeterFrame();
			frame.parse(data, loc, len);
			if(frame.getLen()>0){
				//������Ч����
				String dbuf=new String(frame.getData(),"iso-8859-1");	//��Ӣ���ַ�����
				result=new ArrayList();
				int sindex=0;
				int eindex=0;
				
				sindex=dbuf.indexOf("4.1(");
				if(sindex>=0){//���� �����й����壩
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9011",result);
					}
				}
				sindex=dbuf.indexOf("4.2(");
				if(sindex>=0){//���� �����й���ƽ��
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9012",result);
					}
				}
				sindex=dbuf.indexOf("4.3(");
				if(sindex>=0){//���� �����й����ȣ�
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9013",result);
					}
				}
				
				sindex=dbuf.indexOf("5.1(");
				if(sindex>=0){//���� �����й����壩
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9021",result);
					}
				}
				sindex=dbuf.indexOf("5.2(");
				if(sindex>=0){//���� �����й���ƽ��
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9022",result);
					}
				}
				sindex=dbuf.indexOf("5.3(");
				if(sindex>=0){//���� �����й����ȣ�
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9023",result);
					}
				}
				
				sindex=dbuf.indexOf("6(");
				if(sindex>=0){//���� �����й����ܣ�
					sindex+=2;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9010",result);
					}
				}
				
				sindex=dbuf.indexOf("7(");
				if(sindex>=0){//���� �����й����ܣ�
					sindex+=2;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9020",result);
					}
				}
				
				sindex=dbuf.indexOf("8(");
				if(sindex>=0){//���� �����޹����ܣ�
					sindex+=2;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9110",result);
					}
				}
				sindex=dbuf.indexOf("9(");
				if(sindex>=0){//���� �����޹����ܣ�
					sindex+=2;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"9120",result);
					}
				}
				
				sindex=dbuf.indexOf("12(");
				if(sindex>=0){//���� �����й�����������ͷ���ʱ��
					sindex+=3;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"A010",result);
						
						if(dbuf.substring(eindex+1, eindex+2).equals("(")){//����ʱ��
							sindex=eindex+2;	//���ݿ�ʼλ��
							eindex=dbuf.indexOf(")",sindex+1);
							if(eindex>0){
								dstring=dbuf.substring(sindex, eindex);
								addItem("20"+dstring,"B010",result);
							}
						}
					}					
				}
				
				sindex=dbuf.indexOf("13(");
				if(sindex>=0){//���� �����й�����������ͷ���ʱ��
					sindex+=3;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"A020",result);
						
						if(dbuf.substring(eindex+1, eindex+2).equals("(")){//����ʱ��
							sindex=eindex+2;	//���ݿ�ʼλ��
							eindex=dbuf.indexOf(")",sindex+1);
							if(eindex>0){
								dstring=dbuf.substring(sindex, eindex);
								addItem("20"+dstring,"B020",result);
							}
						}
					}					
				}
				
				sindex=dbuf.indexOf("L.1(");
				if(sindex>=0){//���� �����޹����ܣ�
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"B611",result);
					}
				}
				sindex=dbuf.indexOf("L.2(");
				if(sindex>=0){//���� �����޹����ܣ�
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"B612",result);
					}
				}
				sindex=dbuf.indexOf("L.3(");
				if(sindex>=0){//���� �����޹����ܣ�
					sindex+=4;//���ݿ�ʼλ��
					eindex=dbuf.indexOf(")", sindex);
					if(eindex>0){//��������
						String dstring=dbuf.substring(sindex, eindex);
						//�������ݣ������ִ��п����е�λ
						String val=fixValue(dstring);
						addItem(val,"B613",result);
					}
				}
			}
		}catch(Exception e){
			log.error("���������ӱ��Լ", e);
		}
		
		if(result!=null){
			return result.toArray();
		}
		return null;
	}

	private String fixValue(String val){
		int index=val.indexOf("*");
		if(index>0){
			return val.substring(0, index);
		}
		return val;
	}
	
	private void addItem(String val,String key,List result){
		DataItem item=new DataItem();
		item.addProperty("value",val);
		item.addProperty("datakey",key);
		result.add(item);
	}
}
