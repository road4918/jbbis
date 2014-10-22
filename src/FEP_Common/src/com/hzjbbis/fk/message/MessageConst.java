package com.hzjbbis.fk.message;

public class MessageConst {
	//浙江规约消息常数定义
	/**
	 * 中继
	 */
	public static final byte ZJ_FUNC_RELAY = 0x00;
	/**
	 * 读当前数据
	 */
	public static final byte ZJ_FUNC_READ_CUR = 0x01;
	/**
	 * 读任务数据
	 */
	public static final byte ZJ_FUNC_READ_TASK = 0x02;
	/**
	 * 读编程日志
	 */
	public static final byte ZJ_FUNC_READ_PROG	= 0x04;
	/**
	 * 实时写对象参数
	 */
	public static final byte ZJ_FUNC_WRITE_ROBJ = 0x07;	
	/**
	 * 写对象参数
	 */
	public static final byte ZJ_FUNC_WRITE_OBJ = 0x08;
	/**
	 * 异常告警
	 */
	public static final byte ZJ_FUNC_EXP_ALARM	= 0x09;
	/**
	 * 告警确认
	 */
	public static final byte ZJ_FUNC_ALARM_CONFIRM = 0x0A;
	/**
	 * 用户自定义数据
	 */
	public static final byte ZJ_FUNC_USER_DEFINE = 0x0F;
	public static final byte ZJ_FUNC_LOGIN = 0x21;			//登录
	public static final byte ZJ_FUNC_LOGOUT = 0x22;		//登录退出
	public static final byte ZJ_FUNC_HEART = 0x24;			//心跳检验
	public static final byte ZJ_FUNC_REQ_SMS = 0x28;		//请求发送短信
	public static final byte ZJ_FUNC_RECV_SMS = 0x29;		//收到短信上报
	
	//消息方向定义
	public static final byte ZJ_DIR_DOWN = 0x00;			//由主站发出的命令帧
	public static final byte ZJ_DIR_UP = 0x01;				//由终端发出的应答帧
	
}
