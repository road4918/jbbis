package com.hzjbbis.fk.model;

import java.util.Date;

import com.hzjbbis.fk.utils.CalendarUtil;



/**
 * ͨѶǰ�û��ն˵����ṹ
 */
public class ComRtu {     
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ��λ���� */
    private String deptCode = "";
    /** �ն�ͨѶ��Լ */
    private String rtuProtocol;
    /** �ն��߼���ַ */
    private int rtua;
    /** �ն��߼���ַ��HEX�� */
    private String logicAddress;
    /** ���̱�� */
    private String manufacturer;
    /** �ն�SIM���� */
    private String simNum;
    /** ��ͨѶ�ŵ����� (8010)*/
    private String commType;
    /** ��ͨѶ�ŵ���ַ (8010)*/
    private String commAddress;
    /** ����ͨ������1(8011) */
    private String b1CommType;
    /** ����ͨ����ַ1(8011) */
    private String b1CommAddress;
    /** ����ͨ������2(8012) */
    private String b2CommType;
    /** ����ͨ����ַ2(8012) */
    private String b2CommAddress;
    //�ն˹�������
    /** ��ǰ��ЧGPRS���ص�ַ,��ʽ:IP:PORT */
    private String activeGprs;
    /** ��ǰ��Ч��������Ӧ�ú�,����955983015 */
    private String activeUms;
    /** GPRS/CDMA��ǰ���� */
    private int upGprsFlowmeter;	//����gprs����
    /** ����ͨ����ǰ���� */
    private int upSmsCount;			//����sms����
    private int downGprsFlowmeter;	//����GPRS����
    private int downSmsCount;		//����sms����
    private String upMobile;		//������������ֻ�����
    private long lastGprsTime;		//���gprs����ʱ��
    private long lastSmsTime;		//���SMS����ʱ��
    private int taskCount;			//����������������
    private int upGprsCount;		//����GPRS��������
    private int downGprsCount;		//����GPRS��������
    //�����ն˵�ͨ�Ų�����һ�µĴ���
    private String misSmsAddress;	//���ʲ���һ�µĶ���ͨ��
    private String misGprsAddress;	//���ʲ���һ�µ��ն�GPRS������

    //2008-12-21 ��������
    private String rtuIpAddr;			//�ն�ʵ��IP��ַ
    private String activeSubAppId;		//�ն˶������з��ֵ�SubAppId����ʼ��ֵ�����ն˲�����
    
    //2009-01-26 �������ԣ�֧�ֵ���������������Լ��������ʱ��
    private int heartbeatCount=0;
    private long lastHeartbeat = 0;

    //����Ҫ�־û������ԣ���HeartbeatPersist��ĳ�ʼ�����ء�
    private int heartSavePosition = -1;
    private int flowSavePosition = -1;
    private int rtuSavePosition = -1;
    
    /** �ն����ͨѶʱ�� */
    private long lastIoTime;
    
    //�����ڲ����ԣ�֧���ϴ������ʱ��
    private long lastReqTime = 0;
	/**
	 * @return ���� activeGprs��
	 */
	public String getActiveGprs() {
		return activeGprs;
	}
	/**
	 * @param activeGprs Ҫ���õ� activeGprs��
	 */
	public void setActiveGprs(String activeGprs) {
		this.activeGprs = activeGprs;
	}
	/**
	 * @return ���� activeUms��
	 */
	public String getActiveUms() {
		return activeUms;
	}
	/**
	 * @param activeUms Ҫ���õ� activeUms��
	 */
	public void setActiveUms(String activeUms) {
		this.activeUms = activeUms;
	}
	/**
	 * @return ���� b1CommAddress��
	 */
	public String getB1CommAddress() {
		return b1CommAddress;
	}
	/**
	 * @param commAddress Ҫ���õ� b1CommAddress��
	 */
	public void setB1CommAddress(String commAddress) {
		b1CommAddress = commAddress;
	}
	/**
	 * @return ���� b1CommType��
	 */
	public String getB1CommType() {
		return b1CommType;
	}
	/**
	 * @param commType Ҫ���õ� b1CommType��
	 */
	public void setB1CommType(String commType) {
		b1CommType = commType;
	}
	/**
	 * @return ���� b2CommAddress��
	 */
	public String getB2CommAddress() {
		return b2CommAddress;
	}
	/**
	 * @param commAddress Ҫ���õ� b2CommAddress��
	 */
	public void setB2CommAddress(String commAddress) {
		b2CommAddress = commAddress;
	}
	/**
	 * @return ���� b2CommType��
	 */
	public String getB2CommType() {
		return b2CommType;
	}
	/**
	 * @param commType Ҫ���õ� b2CommType��
	 */
	public void setB2CommType(String commType) {
		b2CommType = commType;
	}
	/**
	 * @return ���� commAddress��
	 */
	public String getCommAddress() {
		return commAddress;
	}
	/**
	 * @param commAddress Ҫ���õ� commAddress��
	 */
	public void setCommAddress(String commAddress) {
		this.commAddress = commAddress;
	}
	/**
	 * @return ���� commType��
	 */
	public String getCommType() {
		return commType;
	}
	/**
	 * @param commType Ҫ���õ� commType��
	 */
	public void setCommType(String commType) {
		this.commType = commType;
	}
	/**
	 * @return ���� curGprsFlowmeter��
	 */
	public int getUpGprsFlowmeter() {
		return upGprsFlowmeter;
	}
	/**
	 * @param curGprsFlowmeter Ҫ���õ� curGprsFlowmeter��
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
	 * @return ���� deptCode��
	 */
	public String getDeptCode() {
		return deptCode;
	}
	/**
	 * @param deptCode Ҫ���õ� deptCode��
	 */
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	/**
	 * @return ���� lastIoTime��
	 */
	public long getLastIoTime() {
		return lastIoTime;
	}
	/**
	 * @param lastIoTime Ҫ���õ� lastIoTime��
	 */
	public void setLastIoTime(long lastIoTime) {
		this.lastIoTime = lastIoTime;
	}
	/**
	 * @return ���� logicAddress��
	 */
	public String getLogicAddress() {
		return logicAddress;
	}
	/**
	 * @param logicAddress Ҫ���õ� logicAddress��
	 */
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	/**
	 * @return ���� manufacturer��
	 */
	public String getManufacturer() {
		return manufacturer;
	}
	/**
	 * @param manufacturer Ҫ���õ� manufacturer��
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	/**
	 * @return ���� rtua��
	 */
	public int getRtua() {
		return rtua;
	}
	/**
	 * @param rtua Ҫ���õ� rtua��
	 */
	public void setRtua(int rtua) {
		this.rtua = rtua;
	}
	/**
	 * @return ���� rtuId��
	 */
	public String getRtuId() {
		return rtuId;
	}
	/**
	 * @param rtuId Ҫ���õ� rtuId��
	 */
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	/**
	 * @return ���� rtuProtocol��
	 */
	public String getRtuProtocol() {
		return rtuProtocol;
	}
	/**
	 * @param rtuProtocol Ҫ���õ� rtuProtocol��
	 */
	public void setRtuProtocol(String rtuProtocol) {
		this.rtuProtocol = rtuProtocol;
	}
	/**
	 * @return ���� simNum��
	 */
	public String getSimNum() {
		if( null != this.upMobile && this.upMobile.length()>=11 )
			return this.upMobile;
		return simNum;
	}
	/**
	 * @param simNum Ҫ���õ� simNum��
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
	    upGprsFlowmeter = 0;	//����gprs����
	    upSmsCount = 0;			//����sms����
	    downGprsFlowmeter = 0;	//����GPRS����
	    downSmsCount = 0;		//����sms����
	    upMobile = null;		//������������ֻ�����
	    lastGprsTime = 0;		//���gprs����ʱ��
	    lastSmsTime = 0;		//���SMS����ʱ��
	    taskCount = 0;			//����������������
	    upGprsCount = 0;		//����GPRS��������
	    downGprsCount = 0;		//����GPRS��������
	    //�����ն˵�ͨ�Ų�����һ�µĴ���
	    misSmsAddress = null;	//���ʲ���һ�µĶ���ͨ��
	    misGprsAddress = null;	//���ʲ���һ�µ��ն�GPRS������
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
