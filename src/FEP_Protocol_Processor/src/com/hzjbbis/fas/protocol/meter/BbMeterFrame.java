package com.hzjbbis.fas.protocol.meter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * @filename	BbMeterFrame.java
 * @auther 		netice
 * @date		2006-6-20 17:54:08
 * @version		1.0
 * TODO
 */
public class BbMeterFrame extends AbstractMeterFrame{
	private final Log log=LogFactory.getLog(BbMeterFrame.class);
	public static final int CHARACTER_HEAD_FLAG=0x68;
	public static final int CHARACTER_TAIL_FLAG=0x16;
	public static final int MINIMUM_FRAME_LENGTH=12;
	public static final int FLAG_ADDRESS_POSITION=0x1;
	public static final int FLAG_DATA_POSITION=0x0A;
	public static final int FLAG_CTRL_POSITION=0x8;
	public static final int FLAG_BLOCK_DATA=0xAA;
	
	private int datalen;		/*���ݳ���*/
	private int pos;			/*��������ʼλ��*/
	private String meteraddr;	/*���ַ*/
	private int ctrl;			/*������*/
	
	public BbMeterFrame(){
		super();
		datalen=0;
		pos=FLAG_DATA_POSITION;
	}
	
	public BbMeterFrame(byte[] data,int loc,int len){
		super();
		parse(data,loc,len);
		pos=FLAG_DATA_POSITION;
	}
	
	public void parse(byte[] data, int loc, int len) {
		int head=loc;
		int rbound=0;
		
		super.clear();
		try{
			if(data!=null){	//���ݷǿ�
				if(data.length>(loc+len)){
					rbound=loc+len;
				}else{
					rbound=data.length;
				}
				if((rbound-loc)>=MINIMUM_FRAME_LENGTH){	//�����㹻��
					while(head<=(rbound-MINIMUM_FRAME_LENGTH)){	//have chance to find frame from rest data
						if(CHARACTER_HEAD_FLAG==(data[head] & 0xff)){
							if(CHARACTER_HEAD_FLAG==(data[head+7] & 0xff)){	//second head flag
								int flen=(data[head+9] & 0xff);	//�ٶ�֡��
								if((head+flen+FLAG_DATA_POSITION+1)<=rbound){		//���ȷ���Ҫ��
									if(CHARACTER_TAIL_FLAG==(data[head+FLAG_DATA_POSITION+flen+1] & 0xff)){	//tail char
										//cheak cs
										if(ParseTool.calculateCS(data,head,flen+FLAG_DATA_POSITION)==data[head+FLAG_DATA_POSITION+flen]){
											start=0;
											this.len=flen+12;
											this.data=new byte[this.len];
											datalen=flen;											
											System.arraycopy(data,head,this.data,start,this.len);
											meteraddr=ParseTool.BytesToHexC( this.data,FLAG_ADDRESS_POSITION,6,(byte)0xAA);	//���ַ
											pos=FLAG_DATA_POSITION;
											ctrl=this.data[FLAG_CTRL_POSITION];
											//���������� -0x33
											adjustData(this.data,pos,datalen,0x33);
											break;
										}
									}
								}
							}
						}
						head++;	//search from next byte
					}
				}
			}
		}catch(Exception e){
			log.error("����֡ʶ��",e);
		}
	}
	
	private void adjustData(byte[] data,int start,int len,int adjust){
		if(data!=null && data.length>=(start+len)){
			for(int i=start;i<start+len;i++){
				data[i]-=adjust;
			}
		}
	}
	
	/**
	 * @return Returns the datalen.
	 */
	public int getDatalen() {
		return datalen;
	}

	/**
	 * @param datalen The datalen to set.
	 */
	public void setDatalen(int datalen) {
		this.datalen = datalen;
	}

	/**
	 * @return Returns the meteraddr.
	 */
	public String getMeteraddr() {
		return meteraddr;
	}

	/**
	 * @param meteraddr The meteraddr to set.
	 */
	public void setMeteraddr(String meteraddr) {
		this.meteraddr = meteraddr;
	}

	/**
	 * @return Returns the pos.
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * @param pos The pos to set.
	 */
	public void setPos(int pos) {
		this.pos = pos;
	}

	/**
	 * @return Returns the ctrl.
	 */
	public int getCtrl() {
		return ctrl;
	}
	
	
}
