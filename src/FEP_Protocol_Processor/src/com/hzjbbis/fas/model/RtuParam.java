package com.hzjbbis.fas.model;

import java.util.Date;


/**
 *@filename	RtuParam.java
 *@auther	netice
 *@date		2007-6-26
 *@version	1.0
 *TODO
 */
public class RtuParam {
	private String rtuid;		/*终端局号*/
	private Date opttime;		/*操作时间*/
	private int curve;			/*功率曲线号*/
	public int getCurve() {
		return curve;
	}
	public Date getOpttime() {
		return opttime;
	}
	public String getRtuid() {
		return rtuid;
	}
	public void setCurve(int curve) {
		this.curve = curve;
	}
	public void setOpttime(Date opttime) {
		this.opttime = opttime;
	}
	public void setRtuid(String rtuid) {
		this.rtuid = rtuid;
	}	
}
