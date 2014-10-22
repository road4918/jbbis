package com.hzjbbis.fas.protocol.codec;

import java.util.ArrayList;
import java.util.List;

/**
 * ��Ϣ����/�����������ġ��������ò��ܽ���������Ż�������
 * @author ������
 */
public class MessageCodecContext {

    /** �������ܽ���������ŵ��ֲ߳̾����� */
    private static final ThreadLocal task = new ThreadLocal();
    /** �������ܽ����ĸ澯�����б���ֲ߳̾����� */
    private static final ThreadLocal codes = new ThreadLocal();
    
    /**
     * ���ò��ܽ����������
     * @param taskNum �����
     */
    public static void setTaskNum(String taskNum) {
        task.set(taskNum);
    }
    
    /**
     * ȡ�ò��ܽ����������
     * @return ����š����δ���ã��򷵻� null
     */
    public static String getTaskNum() {
        return (String) task.get();
    }
    
    /**
     * ȡ�ò��ܽ���������ţ�Ȼ��������������б���������
     * @return ����š����δ���ã��򷵻� null
     */
    public static String pollTaskNum() {
        String taskNum = (String) task.get();
        if (taskNum != null) {
            task.set(null);
        }
        
        return taskNum;
    }
    
    /**
     * ��Ӳ�����ȫ�����ĸ澯����
     * @param alertCode �澯����
     */
    public static void addAlertCode(int alertCode) {
        List alertCodes = (List) codes.get();
        if (alertCodes == null) {
            alertCodes = new ArrayList(1);
            codes.set(alertCodes);
        }
        alertCodes.add(new Integer(alertCode));
    }
    
    /**
     * ȡ�ò�����ȫ�����ĸ澯�����б�
     * @return �澯�����б�[Integer]�����δ���ã��򷵻� null
     */
    public static List getAlertCodes() {
        return (List) codes.get();
    }
    
    /**
     * ȡ�ò�����ȫ�����ĸ澯�����б�Ȼ��������������б���ĸ澯�����б�
     * @return �澯�����б�[Integer]�����δ���ã��򷵻� null
     */
    public static List pollAlertCodes() {
        List alertCodes = (List) codes.get();
        if (alertCodes != null) {
            codes.set(null);
        }
        
        return alertCodes;
    }
}
