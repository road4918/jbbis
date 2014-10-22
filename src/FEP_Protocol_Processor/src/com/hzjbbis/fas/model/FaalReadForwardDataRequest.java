package com.hzjbbis.fas.model;

/**
 * ���м�����
 * @author ������
 */
public class FaalReadForwardDataRequest extends FaalRequest {

    private static final long serialVersionUID = -6840264166731727088L;
    
    /** ������� */
    private String tn;
    /** ��ʱʱ�� */
    private int timeout;
    /** �Ƿ�Ϊ�㲥���� */
    private boolean broadcast = false;
    /** ��ƹ㲥��ַ */
    private String broadcastAddress;
    
    private String fixProto;	/** ָ����ƹ�Լ */
    private String fixAddre;	/** ָ����Ƶ�ַ */
    private String fixPort;		/** ָ����ƶ˿ں� */
    
    public FaalReadForwardDataRequest() {
        super();
        type = FaalRequest.TYPE_READ_FORWARD_DATA;
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
     * @return Returns the timeout.
     */
    public int getTimeout() {
        return timeout;
    }
    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    /**
     * @return Returns the broadcast.
     */
    public boolean isBroadcast() {
        return broadcast;
    }
    /**
     * @param broadcast The broadcast to set.
     */
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }
    /**
     * @return Returns the broadcastAddress.
     */
    public String getBroadcastAddress() {
        return broadcastAddress;
    }
    /**
     * @param broadcastAddress The broadcastAddress to set.
     */
    public void setBroadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

	public String getFixAddre() {
		return fixAddre;
	}

	public String getFixProto() {
		return fixProto;
	}

	public void setFixAddre(String fixAddre) {
		this.fixAddre = fixAddre;
	}

	public void setFixProto(String fixProto) {
		this.fixProto = fixProto;
	}

	public String getFixPort() {
		return fixPort;
	}

	public void setFixPort(String fixPort) {
		this.fixPort = fixPort;
	}
}
