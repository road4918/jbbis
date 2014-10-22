package com.hzjbbis.fas.model;

import java.util.Date;

/**
 * ��������������
 * @author ������
 */
public class FaalReadTaskDataRequest extends FaalRequest {

    private static final long serialVersionUID = -4362982183058685616L;
    
    /** ����� */
    private String taskNum;
    /** ������ʼʱ�� */
    private Date startTime;
    /** ��ʷ���ݵ��� */
    private int count;
    /** ���ݼ������ */
    private int frequence;
    /** �Ƿ���Ҫ���µ��������ݱ��������ٲ���ʷ��������ʱʹ�� */
    private boolean doUpdate;

    
    public FaalReadTaskDataRequest() {
        super();
        type = FaalRequest.TYPE_READ_TASK_DATA;
    }
    
    /**
     * @return Returns the taskNum.
     */
    public String getTaskNum() {
        return taskNum;
    }
    /**
     * @param taskNum The taskNum to set.
     */
    public void setTaskNum(String taskNum) {
        this.taskNum = taskNum;
    }
    /**
     * @return Returns the startTime.
     */
    public Date getStartTime() {
        return startTime;
    }
    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(Date startTime) {
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
     * @return Returns the frequence.
     */
    public int getFrequence() {
        return frequence;
    }
    /**
     * @param frequence The frequence to set.
     */
    public void setFrequence(int frequence) {
        this.frequence = frequence;
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
