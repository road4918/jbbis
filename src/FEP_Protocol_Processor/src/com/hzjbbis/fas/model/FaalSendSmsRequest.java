package com.hzjbbis.fas.model;

import java.util.List;

/**
 * 发送短信请求
 * @author 张文亮
 */
public class FaalSendSmsRequest extends FaalRequest {

    private static final long serialVersionUID = 1559576885378604677L;
    
    /** 手机号码 */
    private String[] mobiles;
    /** 发送内容 */
    private String content;
    /** 建议发送格式 0：文本 1：pdu*/
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
