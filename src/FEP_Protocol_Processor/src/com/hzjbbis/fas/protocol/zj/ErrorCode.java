package com.hzjbbis.fas.protocol.zj;

import com.hzjbbis.fas.model.HostCommand;

/**
 * �㽭��Լ�������
 * @author ������
 */
public abstract class ErrorCode {

    /** ��ȷ���޴��� */
    public static final byte CMD_OK = 0x00;
    /** �м�����û�з��� */
    public static final byte FWD_CMD_NO_RESPONSE = 0x01;
    /** �������ݷǷ� */
    public static final byte ILLEGAL_DATA = 0x02;
    /** ����Ȩ�޲��� */
    public static final byte INVALID_PASSWORD = 0x03;
    /** �޴������� */
    public static final byte NO_DATA = 0x04;
    /** ����ʱ��ʧЧ */
    public static final byte CMD_TIMEOUT = 0x05;
    /** Ŀ���ַ������ */
    public static final byte TARGET_NOT_EXISTS = 0x11;
    /** ����ʧ�� */
    public static final byte CMD_SEND_FAILURE = 0x12;
    /** ����Ϣ̫֡�� */
    public static final byte FRAME_TOO_LONG = 0x13;
    
    // �Զ��������
    /** �ն���Ӧ��ʱ */
    public static final byte RESPONSE_TIMEOUT = (byte) 0xF1;
    /** ��վ����ʧ�ܣ�û����Ч������ͨ�� */
    public static final byte MST_SEND_FAILURE = (byte) 0xF2;
    /** �ն˶Ͽ����� */
    public static final byte RTU_DISCONNECT = (byte) 0xF3;
    /** �Ƿ����� */
    public static final byte ILLEGAL_PACKET = (byte) 0xFF;
    
    /**
     * ���������ת��Ϊ��վ���������״̬����
     * @param errorCode ���������
     * @return ��վ���������״̬����
     */
    public static String toHostCommandStatus(byte errorCode) {
        switch (errorCode) {
            case CMD_OK:
                return HostCommand.STATUS_SUCCESS;                
            case FWD_CMD_NO_RESPONSE:
            	return HostCommand.STATUS_FWD_CMD_NO_RESPONSE;
            case ILLEGAL_DATA:
            	return HostCommand.STATUS_PARA_INVALID;
            case INVALID_PASSWORD:
            	return HostCommand.STATUS_PERMISSION_DENIDE;
            case NO_DATA:
            	return HostCommand.STATUS_ITEM_INVALID;
            case CMD_TIMEOUT:
                return HostCommand.STATUS_TIME_OVER;                
            case TARGET_NOT_EXISTS:
            	return HostCommand.STATUS_TARGET_UNREACHABLE;
            case CMD_SEND_FAILURE:
            	return HostCommand.STATUS_SEND_FAILURE;
            case FRAME_TOO_LONG:
            	return HostCommand.STATUS_SMS_OVERFLOW;
            case MST_SEND_FAILURE:
                return HostCommand.STATUS_COMM_FAILED;
            case ILLEGAL_PACKET:
            	return HostCommand.STATUS_PARSE_ERROR;
            case RESPONSE_TIMEOUT:
                return HostCommand.STATUS_TIMEOUT;                
            default:
                return HostCommand.STATUS_COMM_FAILED;
        }
    }
}
