package com.hzjbbis.fas.protocol.meter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *@filename	SmMeterFrame.java
 *@auther	netice
 *@date		2007-3-4
 *@version	1.0
 *TODO		�����ӱ�֡
 */
public class SmMeterFrame extends AbstractMeterFrame{
	private final Log log=LogFactory.getLog(SmMeterFrame.class);
	
	public static final int SIEMENS_FRAME_HEAD=0x2F;
	
	public void parse(byte[] data, int loc, int len) {
		int head=loc;	//��Ч֡��ʼλ��
		int rbound=0;	//�ұ߽�
		
		super.clear();
		try{
			if(data!=null){	//���ݷǿ�
				if(data.length>(loc+len)){
					rbound=loc+len;
				}else{
					rbound=data.length;
				}
				while(head<rbound){
					//if(SIEMENS_FRAME_HEAD==(data[head] & 0xFF)){
						this.start=0;
						this.len=rbound-head;						
						this.data=new byte[this.len];
						System.arraycopy(data,head,this.data,this.start,this.len);
						break;
					//}
					//head++;
				}
			}
		}catch(Exception e){
			log.error("�����ӱ��Լ����",e);
		}	
	}

}
