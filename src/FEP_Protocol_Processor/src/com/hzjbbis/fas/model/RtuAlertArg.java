package com.hzjbbis.fas.model;

/**
 * �ն˸澯����
 * @author ������
 */
public class RtuAlertArg {

    /** �澯ID */
    private Long alertId;
    /** ������ */
    private String code;
    /** ����ֵ */
    private String value;
    /** ��Ӧ�� */
    private String correlValue;
    
    /**
     * @return Returns the alertId.
     */
    public Long getAlertId() {
        return alertId;
    }
    /**
     * @param alertId The alertId to set.
     */
    public void setAlertId(Long alertId) {
        this.alertId = alertId;
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
     * @return Returns the correlValue.
     */
    public String getCorrelValue() {
        return correlValue;
    }
    /**
     * @param correlValue The correlValue to set.
     */
    public void setCorrelValue(String correlValue) {
        this.correlValue = correlValue;
    }
}
