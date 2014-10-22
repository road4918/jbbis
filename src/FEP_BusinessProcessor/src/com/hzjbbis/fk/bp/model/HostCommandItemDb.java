package com.hzjbbis.fk.bp.model;

import java.util.Date;

/**
 * ��վ������������
 */
public class HostCommandItemDb {
     /** �������ý�������óɹ� */
    public static final int STATUS_SUCCESS = 0;
    /** �������ý������ȷ�� */
    public static final int STATUS_AMBIGUOUS = 1;
    /** �������ý��������ʧ�� */
    public static final int STATUS_FAILED = 2;
    
    /** ����ID */
    private long commandId;
    /** ������� */
    private String tn;
    /** �澯���� */
    private String alertCode;
    /** ��������� */
    private String code;
    /** ����ֵ */
    private String value;
    /** ����ʱ��/�澯ʱ�� */
    private Date time;
    /** ���ʱ�� */
    private Date programTime;
    /** ͨ�� */
    private String channel;
    /** �������ý�� */
    private int status;
    
    /**
     * @return Returns the commandId.
     */
    public Long getCommandId() {
        return commandId;
    }
    /**
     * @param commandId The commandId to set.
     */
    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
    /**
     * @return Returns the tn.
     */
    public String getTn() {
        return tn;
    }
    /**
     * @param tn The tn to set.
     */
    public void setTn(String tn) {
        this.tn = tn;
    }
    /**
     * @return Returns the alertCode.
     */
    public String getAlertCode() {
        return alertCode;
    }
    /**
     * @param alertCode The alertCode to set.
     */
    public void setAlertCode(String alertCode) {
        this.alertCode = alertCode;
    }
    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }
    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    /**
     * @return Returns the time.
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time The time to set.
     */
    public void setTime(Date time) {
        this.time = time;
    }
    /**
     * @return Returns the programTime.
     */
    public Date getProgramTime() {
        return programTime;
    }
    /**
     * @param programTime The programTime to set.
     */
    public void setProgramTime(Date programTime) {
        this.programTime = programTime;
    }
    /**
     * @return Returns the channel.
     */
    public String getChannel() {
        return channel;
    }
    /**
     * @param channel The channel to set.
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }
    /**
     * @return Returns the status.
     */
    public int getStatus() {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(int status) {
        this.status = status;
    }
	public void setCommandId(long commandId) {
		this.commandId = commandId;
	}	 
}
