package com.hzjbbis.fk.fe.filecache;

/**
 * �ն�ͨ�Ų�����һ�¡�
 * �㷨��
 * 		1��gprs/ sms /sim ��������,���Ϊnull�����ʾһ��
 * 		2��
 * @author bhw
 *
 */
public class MisparamRtu {
	private int rtua;		//�ĸ�RTU����
	private String gprsActiveCommAddr;
	private String smsActiveCommAddr;
	private long lastUpdate;		//���һ�θ���ʱ��
	private char state = '0';
	
	public int getRtua() {
		return rtua;
	}
	public void setRtua(int rtua) {
		this.rtua = rtua;
	}

	public String getGprsActiveCommAddr() {
		return gprsActiveCommAddr;
	}
	public void setGprsActiveCommAddr(String gprsActiveCommAddr) {
		this.gprsActiveCommAddr = gprsActiveCommAddr;
		if( null == this.gprsActiveCommAddr )
			this.lastUpdate = 0;
	}

	public String getSmsActiveCommAddr() {
		return smsActiveCommAddr;
	}
	public void setSmsActiveCommAddr(String smsActiveCommAddr) {
		this.smsActiveCommAddr = smsActiveCommAddr;
		if( null == this.smsActiveCommAddr )
			this.lastUpdate = 0;
	}

	public void setLastUpdate(){
		this.lastUpdate = System.currentTimeMillis();
	}
	
	public void setLastUpdate(long tm){
		lastUpdate = tm;
	}
	
	public long getLastUpdate(){
		return this.lastUpdate;
	}
	
	public char getState() {
		return state;
	}
	public void setState(char state) {
		this.state = state;
	}
	
	/**
	 * �ɲ�һ�£������һ�µ��������һ�¶�������ɾ����
	 * @return
	 */
	public boolean isDirty(){
		return null != this.gprsActiveCommAddr || null != this.smsActiveCommAddr ;
	}
}
