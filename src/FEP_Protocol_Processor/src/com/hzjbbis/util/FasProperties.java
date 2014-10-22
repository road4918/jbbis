package com.hzjbbis.util;

import java.util.Calendar;

/**
 * ���� FAS �������ԵĹ�����
 * @author ������
 */
public class FasProperties {

    /** FAS �����ļ��� */
    private static final String PROPS_FILE = "/fas.properties";
    /** �������������ļ�����·�� */
    private static final String PROP_FILE_STORE_PATH = "fas.file.store.path";
    /** ȱʡ���ļ�����·�� */
    private static final String DEFAULT_FILE_STORE_PATH = "fas";
    
    /** FAS ���Լ� */
    private static FileBasedProperties props = null;
    
    /**
     * ���� FAS ���Լ�
     * @return ���Լ�
     */
    public static FileBasedProperties getProperties() {
        if (props == null) {
            synchronized (FasProperties.class) {
                if (props == null) {
                    props = new FileBasedProperties(PROPS_FILE);
                }
            }
        }
        
        return props;
    }
    
    /**
     * ȡ�����ڴ洢ͨѶ����������ļ��ľ���·������������ļ���ָ�������·�����������
     * �û���ǰĿ¼�������·��
     * @return ����·����
     */
    public static String getFileStorePath() {
        String path = getProperties().getProperty(PROP_FILE_STORE_PATH);
        if (path == null || path.trim().length() == 0) {
            path = DEFAULT_FILE_STORE_PATH;
        }
        
        return FileUtil.getAbsolutePath(path);
    }
    
    /**
     * ȡ��ĳ������ֵ
     * @param key ���Եļ���
     * @return ����ֵ�������Բ����ڣ��򷵻� null
     */
    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }
    
    /**
     * ȡ��ĳ������ֵ
     * @param key ���Եļ���
     * @param defaultValue ȱʡֵ
     * @return ����ֵ�������Բ����ڣ��򷵻�ȱʡֵ
     */
    public static String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }
    
    /**
     * ��ȡ������������ֵ
     * @param key ������
     * @param defaultValue ȱʡֵ
     * @return �������͵�����ֵ�����δָ������ֵ���򷵻�ȱʡֵ
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return getProperties().getBoolean(key, defaultValue);
    }
    
    /**
     * ��ȡ��������ֵ
     * @param key ������
     * @param defaultValue ȱʡֵ
     * @return ���ε�����ֵ�����δָ������ֵ���򷵻�ȱʡֵ
     */
    public static int getInt(String key, int defaultValue) {
        return getProperties().getInt(key, defaultValue);
    }
    
    /**
     * ��ȡ��ʾ��С�����ԡ�����ֵ���Բ�ָ����λ��Ҳ����ָ����λ��KB/K��MB/M �� GB/G��
     * @param key ������
     * @param defaultValue ȱʡֵ
     * @return �����ε�����ֵ�����δָ������ֵ���򷵻�ȱʡֵ
     */
    public static long getSize(String key, long defaultValue) {
        return getProperties().getSize(key, defaultValue);
    }

    /**
     * ��ȡ�������͵�����ֵ
     * @param key ������
     * @return �������͵�����ֵ�����δָ������ֵ���򷵻� null
     */
    public static Calendar getDate(String key) {
        return getProperties().getDate(key);
    }
}
