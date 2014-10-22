package com.hzjbbis.fk.fe.filecache;

/**
 * 终端通信参数不一致。
 * 算法：
 * 		1）gprs/ sms /sim 三个参数,如果为null，则表示一致
 * 		2）
 * @author bhw
 *
 */
public class MisparamRtu {
	private int rtua;		//哪个RTU对象
	private String gprsActiveCommAddr;
	private String smsActiveCommAddr;
	private long lastUpdate;		//最近一次更新时间
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
	 * 由不一致，到最后一致的情况，不一致对象允许删除。
	 * @return
	 */
	public boolean isDirty(){
		return null != this.gprsActiveCommAddr || null != this.smsActiveCommAddr ;
	}
}
