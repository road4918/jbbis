package com.hzjbbis.fk.model;

/**
 * 测量点
 * @author 张文亮
 */
public class MeasuredPoint {

    /** 测量点性质：485表计 */
    private static final String TYPE_METER = "01";
    /** CT/PT 的缺省值 */
    private static final int DEFAULT_CT_PT = 1;
    
    /** 终端局号ID */
    private String rtuId;
    /** 户号 */
    private String customerNo;
    /** 单位代码 */
    //private String deptCode;
    /** 测量点号 */
    private String tn;
    /** 测量点局号 */
    private String stationNo;
    /** 测量点性质 */
    private String stationType;
    /** 表计地址 */
    private String atrAddress;
    /** 表计端口号 */
    private String atrPort;
    /** 表计通讯规约 */
    private String atrProtocol;
    /** CT */
    private int ct = DEFAULT_CT_PT;
    /** PT */
    private int pt = DEFAULT_CT_PT;
    /** 数据保存ID */
    private String dataSaveID;
    
    /**
     * 判断测量点是否表计类型
     * @return true - 表计，false - 非表计
     */
    public boolean isMeterType() {
        return TYPE_METER.equals(stationType);
    }
    
    /**
     * 以字符串格式设置 CT 值
     * @param ctStr CT 的字符串形式
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
     * 以字符串格式设置 PT 值
     * @param ptStr PT 的字符串形式
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
	 * @return 返回 atrAddress。
	 */
	public String getAtrAddress() {
		return atrAddress;
	}

	/**
	 * @param atrAddress 要设置的 atrAddress。
	 */
	public void setAtrAddress(String atrAddress) {
		this.atrAddress = atrAddress;
	}

	/**
	 * @return 返回 atrPort。
	 */
	public String getAtrPort() {
		return atrPort;
	}

	/**
	 * @param atrPort 要设置的 atrPort。
	 */
	public void setAtrPort(String atrPort) {
		this.atrPort = atrPort;
	}

	/**
	 * @return 返回 atrProtocol。
	 */
	public String getAtrProtocol() {
		return atrProtocol;
	}

	/**
	 * @param atrProtocol 要设置的 atrProtocol。
	 */
	public void setAtrProtocol(String atrProtocol) {
		this.atrProtocol = atrProtocol;
	}

	/**
	 * @return 返回 customerNo。
	 */
	public String getCustomerNo() {
		return customerNo;
	}

	/**
	 * @param customerNo 要设置的 customerNo。
	 */
	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	/**
	 * @return 返回 dataSaveID。
	 */
	public String getDataSaveID() {
		return dataSaveID;
	}

	/**
	 * @param dataSaveID 要设置的 dataSaveID。
	 */
	public void setDataSaveID(String dataSaveID) {
		this.dataSaveID = dataSaveID;
	}
	
	/**
	 * @return 返回 stationType。
	 */
	public String getStationType() {
		return stationType;
	}

	/**
	 * @param stationType 要设置的 stationType。
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
