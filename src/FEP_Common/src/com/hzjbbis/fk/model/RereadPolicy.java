package com.hzjbbis.fk.model;


/**
 * ���в���
 */
public class RereadPolicy {   
    /** ���в���ID */
    private Integer rereadPolicyID;  
    /** ���м��������Ϊ��λ�� */
    private int rereadInterval;
    /** ����ʱ�䷶Χ������Ϊ��λ��*/
    private int rereadRange;
    /** ���л�׼ʱ�䣨����Ϊ��λ�� */
    private int rereadStartTime;
    /** ������ǰʱ�䣨СʱΪ��λ�� */
    private int rereadAdvanceTime;
    /** ����������־ */
    private boolean rereadStartTag;
    
	public Integer getRereadPolicyID() {
		return rereadPolicyID;
	}
	public void setRereadPolicyID(Integer rereadPolicyID) {
		this.rereadPolicyID = rereadPolicyID;
	}
	public int getRereadInterval() {
		return rereadInterval;
	}
	public void setRereadInterval(int rereadInterval) {
		this.rereadInterval = rereadInterval;
	}
	public int getRereadRange() {
		return rereadRange;
	}
	public void setRereadRange(int rereadRange) {
		this.rereadRange = rereadRange;
	}
	public int getRereadStartTime() {
		return rereadStartTime;
	}
	public void setRereadStartTime(int rereadStartTime) {
		this.rereadStartTime = rereadStartTime;
	}
	public int getRereadAdvanceTime() {
		return rereadAdvanceTime;
	}
	public void setRereadAdvanceTime(int rereadAdvanceTime) {
		this.rereadAdvanceTime = rereadAdvanceTime;
	}
	public boolean isRereadStartTag() {
		return rereadStartTag;
	}
	public void setRereadStartTag(boolean rereadStartTag) {
		this.rereadStartTag = rereadStartTag;
	}
    

   
    
    
    
    
	
  		
}
