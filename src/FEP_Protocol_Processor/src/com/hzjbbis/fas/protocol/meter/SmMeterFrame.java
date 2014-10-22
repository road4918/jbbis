package com.hzjbbis.fas.protocol.meter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *@filename	SmMeterFrame.java
 *@auther	netice
 *@date		2007-3-4
 *@version	1.0
 *TODO		西门子表帧
 */
public class SmMeterFrame extends AbstractMeterFrame{
	private final Log log=LogFactory.getLog(SmMeterFrame.class);
	
	public static final int SIEMENS_FRAME_HEAD=0x2F;
	
	public void parse(byte[] data, int loc, int len) {
		int head=loc;	//有效帧起始位置
		int rbound=0;	//右边界
		
		super.clear();
		try{
			if(data!=null){	//数据非空
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
			log.error("西门子表规约解析",e);
		}	
	}

}
