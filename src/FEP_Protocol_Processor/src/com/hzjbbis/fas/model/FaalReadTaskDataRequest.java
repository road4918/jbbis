package com.hzjbbis.fas.model;

import java.util.Date;

/**
 * 读任务数据请求
 * @author 张文亮
 */
public class FaalReadTaskDataRequest extends FaalRequest {

    private static final long serialVersionUID = -4362982183058685616L;
    
    /** 任务号 */
    private String taskNum;
    /** 数据起始时间 */
    private Date startTime;
    /** 历史数据点数 */
    private int count;
    /** 数据间隔倍率 */
    private int frequence;
    /** 是否需要更新到任务数据表。用于在召测历史任务数据时使用 */
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
