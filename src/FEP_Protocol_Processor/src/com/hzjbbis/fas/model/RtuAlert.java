package com.hzjbbis.fas.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hzjbbis.util.HexDump;

/**
 * �ն˸澯
 * @author ������
 */
public class RtuAlert {

    /** �����־��δ���� */
    public static final String FLAG_UNPROCESSED = "00";
    /** �����־���Ѵ��� */
    public static final String FLAG_PROCESSED = "01";
    
    /** �澯����ID */
    private String dataSaveID;
    /** ��λ���� */
    private String corpNo;
    /** ���� */
    private String customerNo;
    /** �ն�ID */
    private String rtuId;
    /** ������� */
    private String tn;
    /** ������ֺ� */
    private String stationNo;
    /** �澯���� */
    private int alertCode;
    /** �澯���루ʮ�������ַ����� */
    private String alertCodeHex;
    /** �澯����ʱ�� */
    private Date alertTime;
    /** �澯����ʱ�� */
    private Date receiveTime;
    /** �����־ */
    private String processFlag = FLAG_UNPROCESSED;
    /** �澯����[RtuAlertArg] */
    private List args;
    /** �澯��������*/
    private String sbcs;
    /** ͨѶ��ʽ*/
    private String txfs;
    /**
     * ��Ӹ澯����
     * @param arg �澯����
     */
    public void addAlertArg(RtuAlertArg arg) {
        if (args == null) {
            args = new ArrayList();
        }
        args.add(arg);
    }
    
    /**
     * ����ʮ�����Ʊ�ʾ�ĸ澯����
     * @return �澯�����ʮ�����Ʊ�ʾ
     */
    public String getAlertCodeHex() {
        if (alertCodeHex == null) {
            alertCodeHex = HexDump.toHex((short) alertCode);
        }
        return alertCodeHex;
    }
    
   
    public String getDataSaveID() {
		return dataSaveID;
	}

	public void setDataSaveID(String dataSaveID) {
		this.dataSaveID = dataSaveID;
	}

	/**
     * @return Returns the corpNo.
     */
    public String getCorpNo() {
        return corpNo;
    }
    /**
     * @param corpNo The corpNo to set.
     */
    public void setCorpNo(String corpNo) {
        this.corpNo = corpNo;
    }
    /**
     * @return Returns the customerNo.
     */
    public String getCustomerNo() {
        return customerNo;
    }
    /**
     * @param customerNo The customerNo to set.
     */
    public void setCustomerNo(String customerNo) {
        this.customerNo = customerNo;
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
     * @return Returns the alertCode.
     */
    public int getAlertCode() {
        return alertCode;
    }
    /**
     * @param alertCode The alertCode to set.
     */
    public void setAlertCode(int alertCode) {
        this.alertCode = alertCode;
    }
    /**
     * @return Returns the alertTime.
     */
    public Date getAlertTime() {
        return alertTime;
    }
    /**
     * @param alertTime The alertTime to set.
     */
    public void setAlertTime(Date alertTime) {
        this.alertTime = alertTime;
    }
    /**
     * @return Returns the receiveTime.
     */
    public Date getReceiveTime() {
        return receiveTime;
    }
    /**
     * @param receiveTime The receiveTime to set.
     */
    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }
    /**
     * @return Returns the processFlag.
     */
    public String getProcessFlag() {
        return processFlag;
    }
    /**
     * @param processFlag The processFlag to set.
     */
    public void setProcessFlag(String processFlag) {
        this.processFlag = processFlag;
    }
    /**
     * @return Returns the args.
     */
    public List getArgs() {
        return args;
    }
    /**
     * @param args The args to set.
     */
    public void setArgs(List args) {
        this.args = args;
    }

	public String getSbcs() {
		return sbcs;
	}

	public void setSbcs(String sbcs) {
		this.sbcs = sbcs;
	}
    
    
	/**
	 * @return ���� txfs��
	 */
	public String getTxfs() {
		return txfs;
	}
	/**
	 * @param txfs Ҫ���õ� txfs��
	 */
	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}
}
