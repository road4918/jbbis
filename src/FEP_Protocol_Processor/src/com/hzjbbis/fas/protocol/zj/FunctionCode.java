package com.hzjbbis.fas.protocol.zj;

/**
 * �㽭��Լ������
 * @author ������
 */
public abstract class FunctionCode {

    // ��湦����
    /** �����룺���м� */
    public static final int READ_FORWARD_DATA = 0x00;
    /** �����룺����ǰ���� */
    public static final int READ_CURRENT_DATA = 0x01;
    /** �����룺���������� */
    public static final int READ_TASK_DATA = 0x02;
    /** �����룺�������־ */
    public static final int READ_PROGRAM_LOG = 0x04;
    /** �����룺ʵʱд������� */
    public static final int REALTIME_WRITE_PARAMS = 0x07;
    /** �����룺д������� */
    public static final int WRITE_PARAMS = 0x08;
    /** �����룺���澯���� */
    public static final int READ_ALERT = 0x09;
    /** �����룺�澯ȷ�� */
    public static final int CONFIRM_ALERT = 0x0A;
    /** �����룺�û��Զ������� */
    public static final int CUSTOM_DATA = 0x0F;
    /** �����룺��¼ */
    public static final int LOGON = 0x21;
    /** �����룺��¼�˳� */
    public static final int LOGOFF = 0x22;
    /** �����룺�������� */
    public static final int HEART_BEAT = 0x24;
    /** �����룺���Ͷ��� */
    public static final int SEND_SMS = 0x28;
    /** �����룺�յ������ϱ� */
    public static final int RECEIVE_SMS = 0x29;
    /**�����룺��ʷ�����ݲ�ѯ*/
    public static final int HISTORY_DATA = 0x0D;
    /** �����룺�ն˿��� */
    public static final int RTU_CONTROL = 0x42;
    
    // ��չ�����룬����ͨѶ������ǰ�û�֮���ͨѶ
    /** �����룺��ȡ�ն�״̬ */
    public static final int READ_RTU_STATUS = 0x2A;
    /** �����룺�����ŵ�������ֵ/�ϱ��ŵ����� */
    public static final int RTU_THROUGHPUT = 0x2B;
    
    // �Զ��幦���룬�����ڲ�������;���罫��Ϣ�����ԭʼ���Ļ�Ƿ�����
    /** �����룺����Ϊԭʼ���� */
    public static final int DECODE_RAW_DATA = 0xFA;
    /** �����룺����Ϊ�Ƿ����� */
    public static final int DECODE_ILLEGAL_DATA = 0xFB;
    /** �����룺���л���Ϣ */
    public static final int SERIALIZE_MESSAGE = 0xFC;
    /** �����룺����©������ */
    public static final int REREAD_MISSING_DATA = 0xFD;
    /** �����룺ˢ��ͨѶ���񻺴� */
    public static final int REFRESH_CACHE = 0xFE;
    /** �����룺�����Զ������ */
    public static final int OTHER = 0xFF;
    
}