package com.hzjbbis.util;

import java.util.HashMap;
import java.util.Map;

/**
 * ͨѶ�����ṩ������
 * @author ������
 */
public class IspConfig {

    /** ISP ���룺�й���ͨ */
    public static final String ISP_CODE_UNICOM = "01";
    /** ISP ���룺�й��ƶ� */
    public static final String ISP_CODE_MOBILE = "02";
    
    /** ISP ������������ǰ׺ */
    private static final String PROP_PFEFIX = "fas.isp.";
    /** ����������������������� */
    private static final String PROP_MAX_SMSCOUNT = ".maxSmsCount";
    /** �������������������� */
    private static final String PROP_MAX_THROUGHPUT = ".maxThroughput";
    
    /** ���� */
    private static IspConfig instance;
    /** ͨѶ���Ƽ���[ispCode - CommLimit] */
    private Map limits = new HashMap(4);
    
    /**
     * ����һ��ͨѶ�����ṩ������
     */
    private IspConfig() {
        CommLimit limit = readCommLimit(ISP_CODE_UNICOM);
        if (limit != null) {
            limits.put(ISP_CODE_UNICOM, limit);
        }
        
        limit = readCommLimit(ISP_CODE_MOBILE);
        if (limit != null) {
            limits.put(ISP_CODE_MOBILE, limit);
        }
    }
    
    /**
     * ȡ��ͨѶ�����ṩ������ʵ��
     * @return ����ʵ��
     */
    public static IspConfig getInstance() {
        synchronized (IspConfig.class) {
            if (instance == null) {
                instance = new IspConfig();
            }
        }
        return instance;
    }
    
    /**
     * ȡ��ĳ��ͨѶ�ṩ�����������������
     * @param ispCode ͨѶ�ṩ�̱���
     * @return ���������������
     */
    public long getMaxSmsCount(String ispCode) {
        CommLimit limit = (CommLimit) limits.get(ispCode);
        return (limit == null ? 0L : limit.getMaxSmsCount());
    }
    
    /**
     * ȡ��ĳ��ͨѶ�ṩ��������������
     * @param ispCode ͨѶ�ṩ�̱���
     * @return ������������
     */
    public long getMaxThroughput(String ispCode) {
        CommLimit limit = (CommLimit) limits.get(ispCode);
        return (limit == null ? 0L : limit.getMaxThroughput());
    }
    
    /**
     * �������ļ��ж�ȡĳ��ͨѶ�ṩ�̵���������
     * @param ispCode ͨѶ�ṩ�̱���
     * @return ��������
     */
    private CommLimit readCommLimit(String ispCode) {
        CommLimit limit = new CommLimit();
        long l = FasProperties.getSize(PROP_PFEFIX + ispCode + PROP_MAX_SMSCOUNT, 0);
        limit.setMaxSmsCount(l);
        l = FasProperties.getSize(PROP_PFEFIX + ispCode + PROP_MAX_THROUGHPUT, 0);
        limit.setMaxThroughput(l);
        
        return limit;
    }
}

/**
 * ͨѶ����
 * @author ������
 */
class CommLimit {
    
    /** ��������������� */
    private long maxSmsCount;
    /** ��������������Byte�� */
    private long maxThroughput;
    
    /**
     * @return Returns the maxSmsCount.
     */
    public long getMaxSmsCount() {
        return maxSmsCount;
    }
    /**
     * @param maxSmsCount The maxSmsCount to set.
     */
    public void setMaxSmsCount(long maxSmsCount) {
        this.maxSmsCount = maxSmsCount;
    }
    /**
     * @return Returns the maxThroughput.
     */
    public long getMaxThroughput() {
        return maxThroughput;
    }
    /**
     * @param maxThroughput The maxThroughput to set.
     */
    public void setMaxThroughput(long maxThroughput) {
        this.maxThroughput = maxThroughput;
    }
}
