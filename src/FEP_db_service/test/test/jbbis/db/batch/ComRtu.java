package test.jbbis.db.batch;



/**
 * 通讯前置机终端档案结构
 */
public class ComRtu {     
    /** 终端局号ID */
    private String rtuId;
    /** 单位代码 */
    private String deptCode;
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
    /** 当前有效GPRS网关地址,格式:IP:PORT */
    private String activeGprs;
    /** 当前有效短信网关应用号,范例955983015 */
    private String activeUms;
    /** GPRS/CDMA当前流量 */
    private long curGprsFlowmeter;
    /** 短信通道当前条数 */
    private long curSmsCounter;
    /** 终端最近通讯时间 */
    private long lastIoTime;
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
	public long getCurGprsFlowmeter() {
		return curGprsFlowmeter;
	}
	/**
	 * @param curGprsFlowmeter 要设置的 curGprsFlowmeter。
	 */
	public void setCurGprsFlowmeter(long curGprsFlowmeter) {
		this.curGprsFlowmeter = curGprsFlowmeter;
	}
	/**
	 * @return 返回 curSmsCounter。
	 */
	public long getCurSmsCounter() {
		return curSmsCounter;
	}
	/**
	 * @param curSmsCounter 要设置的 curSmsCounter。
	 */
	public void setCurSmsCounter(long curSmsCounter) {
		this.curSmsCounter = curSmsCounter;
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
		return simNum;
	}
	/**
	 * @param simNum 要设置的 simNum。
	 */
	public void setSimNum(String simNum) {
		this.simNum = simNum;
	}
    
       
   
    
	
}
