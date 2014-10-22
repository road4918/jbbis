package com.hzjbbis.fas.model;

import java.util.Calendar;

/**
 * �������־����
 * @author ������
 */
public class FaalReadProgramLogRequest extends FaalRequest {

    private static final long serialVersionUID = -2603907087782103968L;
    
    /** ������š�255 - �ն˺����в����㣬254 - ���в����� */
    private String tn;
    /** ������ʼʱ�� */
    private Calendar startTime;
    /** ��¼�������� */
    private int count;
    
    public FaalReadProgramLogRequest() {
        super();
        type = FaalRequest.TYPE_READ_PROGRAM_LOG;
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
}
