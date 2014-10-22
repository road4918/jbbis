package com.hzjbbis.fk.common;

/**
 * <p>Title: Java Socket Server with NIO support </p>
 * <p>Description: ���ﶨ���¼����ͣ�����֧��socket�����յ����߷��͵�׼����������¼����塣
 * </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author bhw
 * @version 1.0
 */

public class EventType {
	//ϵͳȫ���¼�
	public static final EventType SYS_UNDEFINE = new EventType("δ���������¼�");
	public static final EventType SYS_KILLTHREAD = new EventType("kill thread");
	public static final EventType SYS_EVENT_PROCESS_TIMEOUT = new EventType("event process timeout alarm");
	public static final EventType SYS_TIMER = new EventType("ϵͳ����ʱ���¼�");
	public static final EventType SYS_MEMORY_PROFILE = new EventType("�ڴ������¼�");
	public static final EventType SYS_IDLE = new EventType("ϵͳ����");
	
	// ǰ�û���GPRS/CDMA��������¼�
	public static final EventType FE_RTU_CHANNEL = new EventType("ǰ�û�RTUͨ���ȹ���");
//	public static final EventType FE_RTU_UPMESSAGE = new EventType("RTU up message event");
//	public static final EventType FE_RTU_DOWNMESSAGE = new EventType("RTU down message event");
//	public static final EventType FE_CHANNEL_BUSY	 = new EventType("channel busy event");
	
	//ǰ�û�ҵ�����¼�
	public static final EventType BP_RELAY = new EventType("���м��¼�");
	public static final EventType BP_READ_CURRENT = new EventType("����ǰ����");
	public static final EventType BP_READ_TASK = new EventType("����������");
	public static final EventType BP_READ_PROG = new EventType("�������־");
	public static final EventType BP_WRITE_ROBJ = new EventType("ʵʱд�������");
	public static final EventType BP_WRITE_OBJ = new EventType("д�������");
	public static final EventType BP_EXP_ALARM = new EventType("�쳣�澯");
	public static final EventType BP_ALARM_CONFIRM = new EventType("�澯ȷ��");
	public static final EventType BP_USER_DEFINE = new EventType("�û��Զ�������");
	public static final EventType BP_LOGINOUT = new EventType("��¼���˳�");
	public static final EventType BP_HEART = new EventType("��������");
	public static final EventType BP_REQ_SMS = new EventType("�����Ͷ���");
	public static final EventType BP_RECV_SMS = new EventType("�յ������ϱ�");
	public static final EventType BP_LOG_DB = new EventType("ͨ��ԭʼ����д���ݿ�");
	public static final EventType BP_MAST_REQ = new EventType("��վ��������");
	public static final EventType BP_BATCH_DELAY = new EventType("���������ӳ�ִ��");
	
	//socket server ����¼�
	public static final EventType SERVERPROFILE = new EventType("socket server profile");
	public static final EventType ACCEPTCLIENT = new EventType("accept client");
	public static final EventType CLIENTCLOSE = new EventType("client close");
	public static final EventType CLIENTTIMEOUT = new EventType("client IO time out");
	public static final EventType SERVERSTARTED = new EventType("server start ok");
	public static final EventType SERVERSTOPPED = new EventType("server stopped");

	//socket client����¼�
	public static final EventType CLIENT_WRITE_REQ = new EventType("client write request");
	public static final EventType CLIENT_CONNECTED = new EventType("client connected to server");

	//Message����¼�
	public static final EventType MSG_SEND_FAIL = new EventType("message send failed.");
	public static final EventType MSG_RECV = new EventType("receive message");
	public static final EventType MSG_SENT = new EventType("message been sent");
	public static final EventType MSG_SIMPLE_RECV = new EventType("receive simple message");
	public static final EventType MSG_SIMPLE_SENT = new EventType("simple message been sent");
	public static final EventType MSG_PARSE_ERROR = new EventType("message parse error");
//	public static final EventType MSG_RECV_GPRS = new EventType("receive gprs message");
//	public static final EventType MSG_SENT_GPRS = new EventType("gprs message sent");
//	public static final EventType MSG_RECV_SMS = new EventType("receive sms message");
//	public static final EventType MSG_SENT_SMS = new EventType("sms message sent");

	
	public static final EventType EXCEPTION = new EventType("EXCEPTION");
	public static final EventType MESSAGE_DISCARDED = new EventType("MESSAGE_DISCARDED");

	/** ����д���ݿ���־�¼� */
	public static final EventType LOG_MESSAGE_UP = new EventType("������Ϣ��־");
	public static final EventType LOG_MESSAGE_DOWN = new EventType("������Ϣ��־");

	/** �����ն�FEID�ֶ� */
	public static final EventType UPDATE_FEID = new EventType("�����ն�FEID�ֶ�");

	/** ģ���¼�֪ͨ */
	public static final EventType MODULE_STARTTED = new EventType("ģ�������ɹ�");
	public static final EventType MODULE_STOPPED = new EventType("ģ��ֹͣ");
	public static final EventType MODULE_PROFILE = new EventType("ģ��ͳ����Ϣ");
	public static final EventType DB_AVAILABLE = new EventType("���ݿ����״̬");
	public static final EventType DB_UNAVAILABLE = new EventType("���ݿⲻ����");

	private final String desc;
	private final int index;
	private static int sequence = 0;

	private EventType(String desc) {
		this.desc = desc;
		this.index = sequence++;
	}

	public String toString() {
		return desc;
	}
	
	public int toInt(){
		return index;
	}

	public static int getMaxIndex(){
		return sequence;
	}

	@Override
	public boolean equals(Object obj) {
		try{
			EventType etype = (EventType)obj;
			return index == etype.toInt();
		}catch(Exception e){
			return false;
		}
	}

}