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
	private String rtuid;		/*�ն˾ֺ�*/
	private Date opttime;		/*����ʱ��*/
	private int curve;			/*�������ߺ�*/
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
