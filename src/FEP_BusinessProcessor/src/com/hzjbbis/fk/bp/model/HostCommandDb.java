package com.hzjbbis.fk.bp.model;


/**
 * ��վ���������б���
 */
public class HostCommandDb  {   
    /** ����ID */
    private Long id;
    /** ��Ϣ������������ܱ���ֳɶ����Ϣ�·� */
    private int messageCount;
    /** ����״̬ */
    private String status;
    /** ������ */
    private String errcode;		/*����������*/
      
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
          
	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
		this.status=this.errcode;
	}	
}
