package com.hzjbbis.fk.monitor;

public class MonitorDataItem {
	public double cpuUsage = 0.0;
	public long freeDisk = 0;		//µ¥Î»M
	public long totalMemory = 0;
	public long freeMemory = 0;
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("cpu=").append(cpuUsage).append("%;  ");
		sb.append("freeDisk=").append(freeDisk).append("M;  ");
		sb.append("totalMemory=").append(totalMemory).append("M;  ");
		sb.append("freeMemory=").append(freeMemory).append("M");
		return sb.toString();
	}
}
