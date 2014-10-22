/**
 * ���ϵͳ������塣
 */
package com.hzjbbis.fk.monitor;

/**
 * @author hbao
 * ���ڲ��� ����/Ӧ��ģʽ��Ӧ����������������ͬ��
 * �����н����Ӧ����body����Ϊ������ݡ������޽���������ģ��������ֹͣ,����body���ַ����ַ�����Ϣ��
 */
public interface MonitorCommand {

	public static final short CMD_INVALID = 0x00;		//�Ƿ�����
	
	//��־�������
	public static final short CMD_LOG_LIST 			= 0x01;		//��־�ļ��б�
	//ϵͳԶ�����ù���
	public static final short CMD_CONFIG_LIST		= 0x02;		//�����ļ��б�
	public static final short CMD_GET_FILE 			= 0x03;		//�ļ��ֿ�����
	public static final short CMD_PUT_FILE 			= 0x04;		//�ļ��ϴ���һ�����������ļ��ϴ�
	
	//ģ����
	public static final short CMD_GATHER_PROFILE   	= 0x1f;	//ǰ�û��ɼ��������ӵ�ϵͳ��profile��
	public static final short CMD_SYS_PROFILE 	 	= 0x10;	//ϵͳ�������м�ض����profile xml
	public static final short CMD_MODULE_PROFILE 	= 0x11;	//ģ���profile
	//�¼��������ļ�ء�
	public static final short CMD_EVENT_HOOK_PROFILE= 0x12;	//�¼�������profile

	//ģ�����
	public static final short CMD_MODULE_START	 = 0x13;	//ĳ��ģ������
	public static final short CMD_MODULE_STOP	 = 0x14;	//ĳ��ģ��ֹͣ
	//ϵͳ����
	public static final short CMD_SYS_START 	 = 0x15;	//ϵͳ����
	public static final short CMD_SYS_STOP	 	 = 0x16;	//ϵͳֹͣ
	
	//ĳ���ն˵�ͨ�Ÿ���
	public static final short CMD_TRACE_RTU		 = 0x17;	//�����ն�ͨ�Ÿ���
	public static final short CMD_TRACE_ABORT	 = 0x18;	//ֹͣ�ն�ͨ�Ÿ���
	public static final short CMD_TRACE_IND		 = 0X19;	//�ն�ͨ�Ÿ��ٷ���

}
