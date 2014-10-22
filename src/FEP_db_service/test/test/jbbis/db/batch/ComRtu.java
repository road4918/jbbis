package test.jbbis.db.batch;



/**
 * ͨѶǰ�û��ն˵����ṹ
 */
public class ComRtu {     
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ��λ���� */
    private String deptCode;
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
    /** ��ǰ��ЧGPRS���ص�ַ,��ʽ:IP:PORT */
    private String activeGprs;
    /** ��ǰ��Ч��������Ӧ�ú�,����955983015 */
    private String activeUms;
    /** GPRS/CDMA��ǰ���� */
    private long curGprsFlowmeter;
    /** ����ͨ����ǰ���� */
    private long curSmsCounter;
    /** �ն����ͨѶʱ�� */
    private long lastIoTime;
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
	public long getCurGprsFlowmeter() {
		return curGprsFlowmeter;
	}
	/**
	 * @param curGprsFlowmeter Ҫ���õ� curGprsFlowmeter��
	 */
	public void setCurGprsFlowmeter(long curGprsFlowmeter) {
		this.curGprsFlowmeter = curGprsFlowmeter;
	}
	/**
	 * @return ���� curSmsCounter��
	 */
	public long getCurSmsCounter() {
		return curSmsCounter;
	}
	/**
	 * @param curSmsCounter Ҫ���õ� curSmsCounter��
	 */
	public void setCurSmsCounter(long curSmsCounter) {
		this.curSmsCounter = curSmsCounter;
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
		return simNum;
	}
	/**
	 * @param simNum Ҫ���õ� simNum��
	 */
	public void setSimNum(String simNum) {
		this.simNum = simNum;
	}
    
       
   
    
	
}
