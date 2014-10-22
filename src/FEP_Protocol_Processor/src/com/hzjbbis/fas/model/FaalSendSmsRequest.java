package com.hzjbbis.fas.model;

import java.util.List;

/**
 * ���Ͷ�������
 * @author ������
 */
public class FaalSendSmsRequest extends FaalRequest {

    private static final long serialVersionUID = 1559576885378604677L;
    
    /** �ֻ����� */
    private String[] mobiles;
    /** �������� */
    private String content;
    /** ���鷢�͸�ʽ 0���ı� 1��pdu*/
    private int ctype;
    
    private List<String> smsids;
    
    public FaalSendSmsRequest() {
        super();
        type = FaalRequest.TYPE_SEND_SMS;
    }
    
    /**
     * @return Returns the mobiles.
     */
    public String[] getMobiles() {
        return mobiles;
    }
    /**
     * @param mobiles The mobiles to set.
     */
    public void setMobiles(String[] mobiles) {
        this.mobiles = mobiles;
    }
    /**
     * @return Returns the content.
     */
    public String getContent() {
        return content;
    }
    /**
     * @param content The content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }

	public int getCtype() {
		return ctype;
	}

	public void setCtype(int ctype) {
		this.ctype = ctype;
	}

	public List<String> getSmsids() {
		return smsids;
	}

	public void setSmsids(List<String> smsids) {
		this.smsids = smsids;
	}	
}
