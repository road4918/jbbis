package com.hzjbbis.fk.model;

import java.util.Date;

import com.hzjbbis.fk.utils.CalendarUtil;



/**
 * 通讯前置机终端档案结构
 */
public class ComRtu {     
    /** 终端局号ID */
    private String rtuId;
    /** 单位代码 */
    private String deptCode = "";
    /** 终端通讯规约 */
    private String rtuProtocol;
    /** 终端逻辑地址 */
    private int rtua;
    /** 终端逻辑地址（HEX） */
    private String logicAddress;
    /** 厂商编号 */
    private String manufacturer;
    /** 终端SIM卡号 */
    private String simNum;
    /** 主通讯信道类型 (8010)*/
    private String commType;
    /** 主通讯信道地址 (8010)*/
    private String commAddress;
    /** 备用通道类型1(8011) */
    private String b1CommType;
    /** 备用通道地址1(8011) */
    private String b1CommAddress;
    /** 备用通道类型2(8012) */
    private String b2CommType;
    /** 备用通道地址2(8012) */
    private String b2CommAddress;
    //终端工况定义
    /** 当前有效GPRS网关地址,格式:IP:PORT */
    private String activeGprs;
    /** 当前有效短信网关应用号,范例955983015 */
    private String activeUms;
    /** GPRS/CDMA当前流量 */
    private int upGprsFlowmeter;	//上行gprs流量
    /** 短信通道当前条数 */
    private int upSmsCount;			//上行sms条数
    private int downGprsFlowmeter;	//下行GPRS流量
    private int downSmsCount;		//下行sms条数
    private String upMobile;		//最近短信上行手机号码
    private long lastGprsTime;		//最近gprs上行时间
    private long lastSmsTime;		//最近SMS上行时间
    private int taskCount;			//当天任务上行数量
    private int upGprsCount;		//上行GPRS报文数量
    private int downGprsCount;		//下行GPRS报文数量
    //增加终端的通信参数不一致的处理
    private String misSmsAddress;	//与资产表不一致的短信通道
    private String misGprsAddress;	//与资产表不一致的终端GPRS参数。

    //2008-12-21 增加属性
    private String rtuIpAddr;			//终端实际IP地址
    private String activeSubAppId;		//终端短信上行发现的SubAppId。初始化值来自终端参数表。
    
    //2009-01-26 增加属性，支持当天心跳数量检测以及最近心跳时间
    private int heartbeatCount=0;
    private long lastHeartbeat = 0;

    //不需要持久化的属性，从HeartbeatPersist类的初始化加载。
    private int heartSavePosition = -1;
    private int flowSavePosition = -1;
    private int rtuSavePosition = -1;
    
    /** 终端最近通讯时间 */
    private long lastIoTime;
    
    //增加内部属性，支持上次请求的时间
    private long lastReqTime = 0;
	/**
	 * @return 返回 activeGprs。
	 */
	public String getActiveGprs() {
		return activeGprs;
	}
	/**
	 * @param activeGprs 要设置的 activeGprs。
	 */
	public void setActiveGprs(String activeGprs) {
		this.activeGprs = activeGprs;
	}
	/**
	 * @return 返回 activeUms。
	 */
	public String getActiveUms() {
		return activeUms;
	}
	/**
	 * @param activeUms 要设置的 activeUms。
	 */
	public void setActiveUms(String activeUms) {
		this.activeUms = activeUms;
	}
	/**
	 * @return 返回 b1CommAddress。
	 */
	public String getB1CommAddress() {
		return b1CommAddress;
	}
	/**
	 * @param commAddress 要设置的 b1CommAddress。
	 */
	public void setB1CommAddress(String commAddress) {
		b1CommAddress = commAddress;
	}
	/**
	 * @return 返回 b1CommType。
	 */
	public String getB1CommType() {
		return b1CommType;
	}
	/**
	 * @param commType 要设置的 b1CommType。
	 */
	public void setB1CommType(String commType) {
		b1CommType = commType;
	}
	/**
	 * @return 返回 b2CommAddress。
	 */
	public String getB2CommAddress() {
		return b2CommAddress;
	}
	/**
	 * @param commAddress 要设置的 b2CommAddress。
	 */
	public void setB2CommAddress(String commAddress) {
		b2CommAddress = commAddress;
	}
	/**
	 * @return 返回 b2CommType。
	 */
	public String getB2CommType() {
		return b2CommType;
	}
	/**
	 * @param commType 要设置的 b2CommType。
	 */
	public void setB2CommType(String commType) {
		b2CommType = commType;
	}
	/**
	 * @return 返回 commAddress。
	 */
	public String getCommAddress() {
		return commAddress;
	}
	/**
	 * @param commAddress 要设置的 commAddress。
	 */
	public void setCommAddress(String commAddress) {
		this.commAddress = commAddress;
	}
	/**
	 * @return 返回 commType。
	 */
	public String getCommType() {
		return commType;
	}
	/**
	 * @param commType 要设置的 commType。
	 */
	public void setCommType(String commType) {
		this.commType = commType;
	}
	/**
	 * @return 返回 curGprsFlowmeter。
	 */
	public int getUpGprsFlowmeter() {
		return upGprsFlowmeter;
	}
	/**
	 * @param curGprsFlowmeter 要设置的 curGprsFlowmeter。
	 */
	public void setUpGprsFlowmeter(int curGprsFlowmeter) {
		this.upGprsFlowmeter = curGprsFlowmeter;
	}
	
	public void addUpGprsFlowmeter(int flow){
		this.upGprsFlowmeter += flow;
	}
	
	public void incUpSmsCount(){
		this.upSmsCount++;
	}
	
	public int getUpSmsCount(){
		return this.upSmsCount;
	}
	public void setUpSmsCount(int count){
		this.upSmsCount = count;
	}
	/**
	 * @return 返回 deptCode。
	 */
	public String getDeptCode() {
		return deptCode;
	}
	/**
	 * @param deptCode 要设置的 deptCode。
	 */
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	/**
	 * @return 返回 lastIoTime。
	 */
	public long getLastIoTime() {
		return lastIoTime;
	}
	/**
	 * @param lastIoTime 要设置的 lastIoTime。
	 */
	public void setLastIoTime(long lastIoTime) {
		this.lastIoTime = lastIoTime;
	}
	/**
	 * @return 返回 logicAddress。
	 */
	public String getLogicAddress() {
		return logicAddress;
	}
	/**
	 * @param logicAddress 要设置的 logicAddress。
	 */
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	/**
	 * @return 返回 manufacturer。
	 */
	public String getManufacturer() {
		return manufacturer;
	}
	/**
	 * @param manufacturer 要设置的 manufacturer。
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	/**
	 * @return 返回 rtua。
	 */
	public int getRtua() {
		return rtua;
	}
	/**
	 * @param rtua 要设置的 rtua。
	 */
	public void setRtua(int rtua) {
		this.rtua = rtua;
	}
	/**
	 * @return 返回 rtuId。
	 */
	public String getRtuId() {
		return rtuId;
	}
	/**
	 * @param rtuId 要设置的 rtuId。
	 */
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	/**
	 * @return 返回 rtuProtocol。
	 */
	public String getRtuProtocol() {
		return rtuProtocol;
	}
	/**
	 * @param rtuProtocol 要设置的 rtuProtocol。
	 */
	public void setRtuProtocol(String rtuProtocol) {
		this.rtuProtocol = rtuProtocol;
	}
	/**
	 * @return 返回 simNum。
	 */
	public String getSimNum() {
		if( null != this.upMobile && this.upMobile.length()>=11 )
			return this.upMobile;
		return simNum;
	}
	/**
	 * @param simNum 要设置的 simNum。
	 */
	public void setSimNum(String simNum) {
		this.simNum = simNum;
	}
	public int getDownGprsFlowmeter() {
		return downGprsFlowmeter;
	}
	public void setDownGprsFlowmeter(int downGprsFlowmeter) {
		this.downGprsFlowmeter = downGprsFlowmeter;
	}
	
	public void addDownGprsFlowmeter(int flow){
		this.downGprsFlowmeter += flow;
	}
	
	public int getDownSmsCount() {
		return downSmsCount;
	}
	public void setDownSmsCount(int downSmsCounter) {
		this.downSmsCount = downSmsCounter;
	}
	public void incDownSmsCount(){
		this.downSmsCount++;
	}
	
	public String getUpMobile() {
		if( null != upMobile && upMobile.length()>0 )
			return upMobile;
		else
			return simNum;
	}
	
	public void setUpMobile(String upMobile) {
		this.upMobile = upMobile;
	}
	
	public long getLastGprsTime() {
		return lastGprsTime;
	}
	
	public void setLastGprsTime(long lastGprsTime) {
		this.lastGprsTime = lastGprsTime;
	}
	
	public long getLastSmsTime() {
		return lastSmsTime;
	}
	
	public void setLastSmsTime(long lastSmsTime) {
		this.lastSmsTime = lastSmsTime;
	}
	
	public int getTaskCount() {
		return taskCount;
	}
	
	public int getHasTask(){
		return taskCount !=0 ? 1 : 0;
	}
	
	public String getTaskCountString(){
		return String.valueOf(taskCount);
	}
	
	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}
	
	public void incTaskCount(){
		this.taskCount++;
	}
	
	public int getUpGprsCount() {
		return upGprsCount;
	}
	public void setUpGprsCount(int upGprsCount) {
		this.upGprsCount = upGprsCount;
	}
	
	public void incUpGprsCount(){
		this.upGprsCount++;
	}
	
	public int getDownGprsCount() {
		return downGprsCount;
	}
	
	public void setDownGprsCount(int downGprsCount) {
		this.downGprsCount = downGprsCount;
	}
	public void incDownGprsCount(){
		this.downGprsCount++;
	}
	public String getMisSmsAddress() {
		return misSmsAddress;
	}
	public void setMisSmsAddress(String misSmsAddress) {
		this.misSmsAddress = misSmsAddress;
	}
	public String getMisGprsAddress() {
		return misGprsAddress;
	}
	public void setMisGprsAddress(String misGprsAddress) {
		this.misGprsAddress = misGprsAddress;
	}
	
	public void clearStatus(){
	    upGprsFlowmeter = 0;	//上行gprs流量
	    upSmsCount = 0;			//上行sms条数
	    downGprsFlowmeter = 0;	//下行GPRS流量
	    downSmsCount = 0;		//下行sms条数
	    upMobile = null;		//最近短信上行手机号码
	    lastGprsTime = 0;		//最近gprs上行时间
	    lastSmsTime = 0;		//最近SMS上行时间
	    taskCount = 0;			//当天任务上行数量
	    upGprsCount = 0;		//上行GPRS报文数量
	    downGprsCount = 0;		//下行GPRS报文数量
	    //增加终端的通信参数不一致的处理
	    misSmsAddress = null;	//与资产表不一致的短信通道
	    misGprsAddress = null;	//与资产表不一致的终端GPRS参数。
	    heartbeatCount = 0;
	    lastHeartbeat = 0;
	}
	
	public void setLastReqTime(long lastReqTime) {
		this.lastReqTime = lastReqTime;
	}
	public long getLastReqTime() {
		return lastReqTime;
	}
	
	public Date getLastGprsRecvTime(){
		if( 0 != lastGprsTime )
			return new Date(lastGprsTime);
		else
			return null;
	}
	
	public Date getLastSmsRecvTime(){
		if( 0 != lastSmsTime )
			return new Date(lastSmsTime);
		else
			return null;
	}
	
	public String getDateString(){
		return CalendarUtil.getDateString(System.currentTimeMillis());
	}
	public String getRtuIpAddr() {
		return rtuIpAddr;
	}
	public void setRtuIpAddr(String rtuIpAddr) {
		this.rtuIpAddr = rtuIpAddr;
	}
	public String getActiveSubAppId() {
		return activeSubAppId;
	}
	public void setActiveSubAppId(String activeSubAppId) {
		this.activeSubAppId = activeSubAppId;
	}
	
	public final int getHeartbeatCount() {
		return heartbeatCount;
	}
	public final void setHeartbeatCount(int heartbeatCount) {
		this.heartbeatCount = heartbeatCount;
	}
	public final void incHeartbeat(){
		++heartbeatCount;
	}
	
	public final long getLastHeartbeat() {
		return lastHeartbeat;
	}
	
	public Date getLastHeartbeatTime(){
		if( 0 != lastHeartbeat )
			return new Date(lastHeartbeat);
		else
			return null;
	}
	
	public final void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}
	public final int getHeartSavePosition() {
		return heartSavePosition;
	}
	public final void setHeartSavePosition(int heartSavePosition) {
		this.heartSavePosition = heartSavePosition;
	}
	public final int getFlowSavePosition() {
		return flowSavePosition;
	}
	public final void setFlowSavePosition(int flowSavePosition) {
		this.flowSavePosition = flowSavePosition;
	}
	public final int getRtuSavePosition() {
		return rtuSavePosition;
	}
	public final void setRtuSavePosition(int rtuSavePosition) {
		this.rtuSavePosition = rtuSavePosition;
	}
}
