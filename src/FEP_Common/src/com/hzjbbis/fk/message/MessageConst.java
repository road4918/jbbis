package com.hzjbbis.fk.message;

public class MessageConst {
	//�㽭��Լ��Ϣ��������
	/**
	 * �м�
	 */
	public static final byte ZJ_FUNC_RELAY = 0x00;
	/**
	 * ����ǰ����
	 */
	public static final byte ZJ_FUNC_READ_CUR = 0x01;
	/**
	 * ����������
	 */
	public static final byte ZJ_FUNC_READ_TASK = 0x02;
	/**
	 * �������־
	 */
	public static final byte ZJ_FUNC_READ_PROG	= 0x04;
	/**
	 * ʵʱд�������
	 */
	public static final byte ZJ_FUNC_WRITE_ROBJ = 0x07;	
	/**
	 * д�������
	 */
	public static final byte ZJ_FUNC_WRITE_OBJ = 0x08;
	/**
	 * �쳣�澯
	 */
	public static final byte ZJ_FUNC_EXP_ALARM	= 0x09;
	/**
	 * �澯ȷ��
	 */
	public static final byte ZJ_FUNC_ALARM_CONFIRM = 0x0A;
	/**
	 * �û��Զ�������
	 */
	public static final byte ZJ_FUNC_USER_DEFINE = 0x0F;
	public static final byte ZJ_FUNC_LOGIN = 0x21;			//��¼
	public static final byte ZJ_FUNC_LOGOUT = 0x22;		//��¼�˳�
	public static final byte ZJ_FUNC_HEART = 0x24;			//��������
	public static final byte ZJ_FUNC_REQ_SMS = 0x28;		//�����Ͷ���
	public static final byte ZJ_FUNC_RECV_SMS = 0x29;		//�յ������ϱ�
	
	//��Ϣ������
	public static final byte ZJ_DIR_DOWN = 0x00;			//����վ����������֡
	public static final byte ZJ_DIR_UP = 0x01;				//���ն˷�����Ӧ��֡
	
}
