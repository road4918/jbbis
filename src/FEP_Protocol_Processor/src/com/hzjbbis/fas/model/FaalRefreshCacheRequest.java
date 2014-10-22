package com.hzjbbis.fas.model;

/**
 * ˢ��ͨѶ���񻺴� ͨѶ����
 * @author ������
 */
public class FaalRefreshCacheRequest extends FaalRequest {

    private static final long serialVersionUID = -5642795098417472194L;
    
    /** ����š���ѡ�����ָ������ţ���ֻˢ�¸��������� */
    private String taskNum;
    
    public FaalRefreshCacheRequest() {
        super();
        type = FaalRequest.TYPE_REFRESH_CACHE;
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
}
