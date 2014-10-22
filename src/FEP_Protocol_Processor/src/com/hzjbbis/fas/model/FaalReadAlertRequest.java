package com.hzjbbis.fas.model;

import java.util.Calendar;

/**
 * ���ն˸澯��������
 * @author ������
 */
public class FaalReadAlertRequest extends FaalRequest {

    private static final long serialVersionUID = -654318745767829688L;
    
    /** ������� */
    private String tn;
    /** �澯��ʼʱ�� */
    private Calendar startTime;
    /** �澯���ݵ��� */
    private int count;
    /** �Ƿ���Ҫ���µ��澯�� */
    private boolean doUpdate;
    
    public FaalReadAlertRequest() {
        super();
        type = FaalRequest.TYPE_READ_ALERT;
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
     * @return Returns the startTime.
     */
    public Calendar getStartTime() {
        return startTime;
    }
    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }
    /**
     * @return Returns the count.
     */
    public int getCount() {
        return count;
    }
    /**
     * @param count The count to set.
     */
    public void setCount(int count) {
        this.count = count;
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
}
