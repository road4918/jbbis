package com.hzjbbis.fas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * ��վ��������
 * @author ������
 */
public class HostCommand implements Serializable {

    private static final long serialVersionUID = -4703256789407620876L;
    
    /** ��վ��������״̬: ������ */
    public static final String STATUS_RUNNING = "0";
    /** ��վ��������״̬: �ѽ��� */
    public static final String STATUS_SUCCESS = "1";
    /** ��վ��������״̬: �ն�ִ��ʧ�� */
    public static final String STATUS_RTU_FAILED = "2";
    /** ��վ��������״̬: ͨ������ */
    public static final String STATUS_COMM_FAILED = "3";
    /** ��վ��������״̬: �ն˳�ʱ��Ӧ�� */
    public static final String STATUS_TIMEOUT = "4";
    /** ��վ��������״̬: �м�������Ӧ�� */
    public static final String STATUS_FWD_CMD_NO_RESPONSE = "5";
    /** ��վ��������״̬: �������ݷǷ� */
    public static final String STATUS_PARA_INVALID = "6";
    /** ��վ��������״̬: Ȩ�޲��� */
    public static final String STATUS_PERMISSION_DENIDE = "7";
    /** ��վ��������״̬: �޴������� */
    public static final String STATUS_ITEM_INVALID = "8";
    /** ��վ��������״̬: ����ʱ��ʧЧ */
    public static final String STATUS_TIME_OVER = "9";
    /** ��վ��������״̬: Ŀ���ַ������ */
    public static final String STATUS_TARGET_UNREACHABLE = "10";
    /** ��վ��������״̬: ����ʧ�� */
    public static final String STATUS_SEND_FAILURE = "11";
    /** ��վ��������״̬: ����̫֡�� */
    public static final String STATUS_SMS_OVERFLOW = "12";    
    /** ��վ��������״̬: ��վ�������������֡ʧ�� */
    public static final String STATUS_PRAR_ERROR = "13";   
    /** ��վ��������״̬: ǰ�û�����ʧ�� */
    public static final String STATUS_PARSE_ERROR = "14";    
    /** ��վ��������״̬: ����������ʧ�� */
    public static final String STATUS_TODB_ERROR = "15";
    
    
    /** ����ID */
    private Long id;
    /** ��վ��������ID */
    private Long taskId;
    /** �ն�ID */
    private String rtuId;
    /** �������� */
    private int paramCount;
    /** ��Ϣ������������ܱ���ֳɶ����Ϣ�·� */
    private int messageCount;
    /** �·�ʱ�� */
    private Date requestTime;
    /** ��Ӧʱ�� */
    private Date responseTime;
    /** ����״̬ */
    private String status;
    /** �Ƿ���Ҫ���µ��������ݻ�澯���������ٲ���ʷ�������ݻ�澯ʱʹ�� */
    private boolean doUpdate = false;
    /** �Ƿ�Ϊд������������д�������ͨѶ�������յ�Ӧ�����Ҫ��һ���Ĵ��� */
    private boolean writeParams = false;
    /** ������������ FAAL ������� */
    private FaalRequest request;

    /** �������б�[HostCommandResult] */
    private List<HostCommandResult> results;
    /** ����������[HostCommandMeasurePoint] */
    private List measurePoints;
    private HashMap mpLines;
    private String errcode;		/*����������*/
    
    /**
     * ���������
     * @param result ������
     */
    public void addResult(HostCommandResult result) {
        if (results == null) {
            results = new ArrayList();
        }
        results.add(result);
    }
    
    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * @return Returns the taskId.
     */
    public Long getTaskId() {
        return taskId;
    }
    /**
     * @param taskId The taskId to set.
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    /**
     * @return Returns the rtuId.
     */
    public String getRtuId() {
        return rtuId;
    }
    /**
     * @param rtuId The rtuId to set.
     */
    public void setRtuId(String rtuId) {
        this.rtuId = rtuId;
    }
    /**
     * @return Returns the paramCount.
     */
    public int getParamCount() {
        return paramCount;
    }
    /**
     * @param paramCount The paramCount to set.
     */
    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }
    /**
     * @return Returns the messageCount.
     */
    public int getMessageCount() {
        return messageCount;
    }
    /**
     * @param messageCount The messageCount to set.
     */
    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }
    /**
     * @return Returns the requestTime.
     */
    public Date getRequestTime() {
        return requestTime;
    }
    /**
     * @param requestTime The requestTime to set.
     */
    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }
    /**
     * @return Returns the responseTime.
     */
    public Date getResponseTime() {
        return responseTime;
    }
    /**
     * @param responseTime The responseTime to set.
     */
    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }
    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
        this.errcode=status;
    }
    /**
     * @return Returns the doUpdate.
     */
    public boolean isDoUpdate() {
        return doUpdate;
    }
    /**
     * @param doUpdate The doUpdate to set.
     */
    public void setDoUpdate(boolean doUpdate) {
        this.doUpdate = doUpdate;
    }
    /**
     * @return Returns the writeParams.
     */
    public boolean isWriteParams() {
        return writeParams;
    }
    /**
     * @param writeParams The writeParams to set.
     */
    public void setWriteParams(boolean writeParams) {
        this.writeParams = writeParams;
    }
    /**
     * @return Returns the request.
     */
    public FaalRequest getRequest() {
        return request;
    }
    /**
     * @param request The request to set.
     */
    public void setRequest(FaalRequest request) {
        this.request = request;
    }
   
    /**
     * @return Returns the results.
     */
    public List<HostCommandResult> getResults() {
        return results;
    }
    /**
     * @param results The results to set.
     */
    public void setResults(List<HostCommandResult> results) {
        this.results = results;
    }
    /**
     * @return Returns the measurePoints.
     */
    public List getMeasurePoints() {
        return measurePoints;
    }
    /**
     * @param measurePoints The measurePoints to set.
     */
    public void setMeasurePoints(List measurePoints) {
        this.measurePoints = measurePoints;
    }

	public HashMap getMpLines() {
		return mpLines;
	}

	public void setMpLines(HashMap mpLines) {
		this.mpLines = mpLines;
	}
    
	/**
	 * add mp to line map
	 * @param mp		�ն˲������
	 * @param line		·��
	 */
    public void addLine(String mp,String line){
    	if(mpLines==null){
    		mpLines=new HashMap();
    	}
    	mpLines.put(line, mp);
    }
    
    /**
     * get mp by line
     * @param line
     */
    public String getMpByLine(String line){
    	if(mpLines!=null){
    		if(mpLines.containsKey(line)){
    			return (String)mpLines.get(line);
    		}
    	}
    	return null;
    }

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
		this.status=this.errcode;
	}
}
