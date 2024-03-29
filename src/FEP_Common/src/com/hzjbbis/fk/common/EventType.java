package com.hzjbbis.fk.common;

/**
 * <p>Title: Java Socket Server with NIO support </p>
 * <p>Description: 这里定义事件类型，用来支持socket数据收到或者发送的准备或者完成事件定义。
 * </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author bhw
 * @version 1.0
 */

public class EventType {
	//系统全局事件
	public static final EventType SYS_UNDEFINE = new EventType("未定义类型事件");
	public static final EventType SYS_KILLTHREAD = new EventType("kill thread");
	public static final EventType SYS_EVENT_PROCESS_TIMEOUT = new EventType("event process timeout alarm");
	public static final EventType SYS_TIMER = new EventType("系统级定时器事件");
	public static final EventType SYS_MEMORY_PROFILE = new EventType("内存剖面事件");
	public static final EventType SYS_IDLE = new EventType("系统空闲");
	
	// 前置机：GPRS/CDMA网关相关事件
	public static final EventType FE_RTU_CHANNEL = new EventType("前置机RTU通道等工况");
//	public static final EventType FE_RTU_UPMESSAGE = new EventType("RTU up message event");
//	public static final EventType FE_RTU_DOWNMESSAGE = new EventType("RTU down message event");
//	public static final EventType FE_CHANNEL_BUSY	 = new EventType("channel busy event");
	
	//前置机业务处理事件
	public static final EventType BP_RELAY = new EventType("读中继事件");
	public static final EventType BP_READ_CURRENT = new EventType("读当前数据");
	public static final EventType BP_READ_TASK = new EventType("读任务数据");
	public static final EventType BP_READ_PROG = new EventType("读编程日志");
	public static final EventType BP_WRITE_ROBJ = new EventType("实时写对象参数");
	public static final EventType BP_WRITE_OBJ = new EventType("写对象参数");
	public static final EventType BP_EXP_ALARM = new EventType("异常告警");
	public static final EventType BP_ALARM_CONFIRM = new EventType("告警确认");
	public static final EventType BP_USER_DEFINE = new EventType("用户自定义数据");
	public static final EventType BP_LOGINOUT = new EventType("登录、退出");
	public static final EventType BP_HEART = new EventType("心跳检验");
	public static final EventType BP_REQ_SMS = new EventType("请求发送短信");
	public static final EventType BP_RECV_SMS = new EventType("收到短信上报");
	public static final EventType BP_LOG_DB = new EventType("通信原始报文写数据库");
	public static final EventType BP_MAST_REQ = new EventType("主站下行请求");
	public static final EventType BP_BATCH_DELAY = new EventType("批量保存延迟执行");
	
	//socket server 相关事件
	public static final EventType SERVERPROFILE = new EventType("socket server profile");
	public static final EventType ACCEPTCLIENT = new EventType("accept client");
	public static final EventType CLIENTCLOSE = new EventType("client close");
	public static final EventType CLIENTTIMEOUT = new EventType("client IO time out");
	public static final EventType SERVERSTARTED = new EventType("server start ok");
	public static final EventType SERVERSTOPPED = new EventType("server stopped");

	//socket client相关事件
	public static final EventType CLIENT_WRITE_REQ = new EventType("client write request");
	public static final EventType CLIENT_CONNECTED = new EventType("client connected to server");

	//Message相关事件
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

	/** 报文写数据库日志事件 */
	public static final EventType LOG_MESSAGE_UP = new EventType("上行消息日志");
	public static final EventType LOG_MESSAGE_DOWN = new EventType("下行消息日志");

	/** 更新终端FEID字段 */
	public static final EventType UPDATE_FEID = new EventType("更新终端FEID字段");

	/** 模块事件通知 */
	public static final EventType MODULE_STARTTED = new EventType("模块启动成功");
	public static final EventType MODULE_STOPPED = new EventType("模块停止");
	public static final EventType MODULE_PROFILE = new EventType("模块统计信息");
	public static final EventType DB_AVAILABLE = new EventType("数据库可用状态");
	public static final EventType DB_UNAVAILABLE = new EventType("数据库不可用");

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