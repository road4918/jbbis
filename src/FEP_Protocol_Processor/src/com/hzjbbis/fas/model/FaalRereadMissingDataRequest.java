package com.hzjbbis.fas.model;

import java.util.Date;
import com.hzjbbis.fas.model.*;

/**
 * 漏点补召请求
 * @author 张文亮
 */
public class FaalRereadMissingDataRequest extends FaalRequest {

    private static final long serialVersionUID = -2532892536447853984L;
    
    /** 终端ID */
    private String rtuId;
    /** 任务号 */
    private String taskNum;
    /** 开始时间 */
    private Date startTime;
    /** 结束时间 */
    private Date endTime;
    /** 漏点记录ID数组 */
    private Long[] pointIds;
    
    public FaalRereadMissingDataRequest() {
        super();
        type = FaalRequest.TYPE_REREAD_MISSING_DATA;
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
     * @return Returns the endTime.
     */
    public Date getEndTime() {
        return endTime;
    }
    /**
     * @param endTime The endTime to set.
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    /**
     * @return Returns the pointIds.
     */
    public Long[] getPointIds() {
        return pointIds;
    }
    /**
     * @param pointIds The pointIds to set.
     */
    public void setPointIds(Long[] pointIds) {
        this.pointIds = pointIds;
    }
}
