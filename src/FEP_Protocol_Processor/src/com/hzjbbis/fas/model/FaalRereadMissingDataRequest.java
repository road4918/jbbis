package com.hzjbbis.fas.model;

import java.util.Date;
import com.hzjbbis.fas.model.*;

/**
 * ©�㲹������
 * @author ������
 */
public class FaalRereadMissingDataRequest extends FaalRequest {

    private static final long serialVersionUID = -2532892536447853984L;
    
    /** �ն�ID */
    private String rtuId;
    /** ����� */
    private String taskNum;
    /** ��ʼʱ�� */
    private Date startTime;
    /** ����ʱ�� */
    private Date endTime;
    /** ©���¼ID���� */
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
