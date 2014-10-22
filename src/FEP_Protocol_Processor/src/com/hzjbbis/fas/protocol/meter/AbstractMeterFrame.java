package com.hzjbbis.fas.protocol.meter;

/**
 * ��֡������
 * @author netice
 *
 */
public abstract class AbstractMeterFrame {
	protected int start;		/*֡��ʼλ��*/
	protected int len;		/*֡����*/
	protected byte[] data;	/*֡����,������������*/
	
	public AbstractMeterFrame(){
		this(0,-1,new byte[0]);
	}
	
	public AbstractMeterFrame(int start,int len,byte[] data){
		this.data=data;
		this.len=len;
		this.start=start;
	}
	
	/**
	 * ���Լ֡ʶ����-----�Ӷ������ֽ�������ʶ���Լ֡
	 * @param data
	 */
	public abstract void parse(byte[] data,int loc,int len);
	
	/**
	 * @return Returns the data.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return Returns the len.
	 */
	public int getLen() {
		return len;
	}

	/**
	 * @return Returns the start.
	 */
	public int getStart() {
		return start;
	}
	
	public void clear(){
		data=null;
		start=0;
		len=0;
	}
}
