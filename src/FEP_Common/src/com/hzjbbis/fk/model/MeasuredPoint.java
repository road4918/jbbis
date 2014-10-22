package com.hzjbbis.fk.model;

/**
 * ������
 * @author ������
 */
public class MeasuredPoint {

    /** ���������ʣ�485��� */
    private static final String TYPE_METER = "01";
    /** CT/PT ��ȱʡֵ */
    private static final int DEFAULT_CT_PT = 1;
    
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ���� */
    private String customerNo;
    /** ��λ���� */
    //private String deptCode;
    /** ������� */
    private String tn;
    /** ������ֺ� */
    private String stationNo;
    /** ���������� */
    private String stationType;
    /** ��Ƶ�ַ */
    private String atrAddress;
    /** ��ƶ˿ں� */
    private String atrPort;
    /** ���ͨѶ��Լ */
    private String atrProtocol;
    /** CT */
    private int ct = DEFAULT_CT_PT;
    /** PT */
    private int pt = DEFAULT_CT_PT;
    /** ���ݱ���ID */
    private String dataSaveID;
    
    /**
     * �жϲ������Ƿ�������
     * @return true - ��ƣ�false - �Ǳ��
     */
    public boolean isMeterType() {
        return TYPE_METER.equals(stationType);
    }
    
    /**
     * ���ַ�����ʽ���� CT ֵ
     * @param ctStr CT ���ַ�����ʽ
     */
    public void setCtStr(String ctStr) {
        if (ctStr == null) {
            ct = DEFAULT_CT_PT;
        }
        else {
            try {
                ct = Integer.parseInt(ctStr);
            }
            catch (Exception ex) {
                ct = DEFAULT_CT_PT;
            }
        }
    }
    
    /**
     * ���ַ�����ʽ���� PT ֵ
     * @param ptStr PT ���ַ�����ʽ
     */
    public void setPtStr(String ptStr) {
        if (ptStr == null) {
            pt = DEFAULT_CT_PT;
        }
        else {
            try {
                pt = Integer.parseInt(ptStr);
            }
            catch (Exception ex) {
                pt = DEFAULT_CT_PT;
            }
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[rtuId=").append(rtuId)
            .append(", tn=").append(tn)
            .append(", stationNo=").append(stationNo)
            .append(", type=").append(stationType)
            .append(", address=").append(atrAddress)
            .append(", port=").append(atrPort)
            .append(", protocol=").append(atrProtocol).append("]");
        return sb.toString();
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
	 * @return ���� atrAddress��
	 */
	public String getAtrAddress() {
		return atrAddress;
	}

	/**
	 * @param atrAddress Ҫ���õ� atrAddress��
	 */
	public void setAtrAddress(String atrAddress) {
		this.atrAddress = atrAddress;
	}

	/**
	 * @return ���� atrPort��
	 */
	public String getAtrPort() {
		return atrPort;
	}

	/**
	 * @param atrPort Ҫ���õ� atrPort��
	 */
	public void setAtrPort(String atrPort) {
		this.atrPort = atrPort;
	}

	/**
	 * @return ���� atrProtocol��
	 */
	public String getAtrProtocol() {
		return atrProtocol;
	}

	/**
	 * @param atrProtocol Ҫ���õ� atrProtocol��
	 */
	public void setAtrProtocol(String atrProtocol) {
		this.atrProtocol = atrProtocol;
	}

	/**
	 * @return ���� customerNo��
	 */
	public String getCustomerNo() {
		return customerNo;
	}

	/**
	 * @param customerNo Ҫ���õ� customerNo��
	 */
	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	/**
	 * @return ���� dataSaveID��
	 */
	public String getDataSaveID() {
		return dataSaveID;
	}

	/**
	 * @param dataSaveID Ҫ���õ� dataSaveID��
	 */
	public void setDataSaveID(String dataSaveID) {
		this.dataSaveID = dataSaveID;
	}
	
	/**
	 * @return ���� stationType��
	 */
	public String getStationType() {
		return stationType;
	}

	/**
	 * @param stationType Ҫ���õ� stationType��
	 */
	public void setStationType(String stationType) {
		this.stationType = stationType;
	}

	/**
     * @return Returns the stationNo.
     */
    public String getStationNo() {
        return stationNo;
    }
    /**
     * @param stationNo The stationNo to set.
     */
    public void setStationNo(String stationNo) {
        this.stationNo = stationNo;
    }
    /**
     * @return Returns the ct.
     */
    public int getCt() {
        return ct;
    }
    /**
     * @param ct The ct to set.
     */
    public void setCt(int ct) {
        this.ct = ct;
    }
    /**
     * @return Returns the pt.
     */
    public int getPt() {
        return pt;
    }
    /**
     * @param pt The pt to set.
     */
    public void setPt(int pt) {
        this.pt = pt;
    }
   
}
