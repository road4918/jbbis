package com.hzjbbis.fas.protocol.zj;

/**
 * 浙江规约功能码
 * @author 张文亮
 */
public abstract class FunctionCode {

    // 浙规功能码
    /** 功能码：读中继 */
    public static final int READ_FORWARD_DATA = 0x00;
    /** 功能码：读当前数据 */
    public static final int READ_CURRENT_DATA = 0x01;
    /** 功能码：读任务数据 */
    public static final int READ_TASK_DATA = 0x02;
    /** 功能码：读编程日志 */
    public static final int READ_PROGRAM_LOG = 0x04;
    /** 功能码：实时写对象参数 */
    public static final int REALTIME_WRITE_PARAMS = 0x07;
    /** 功能码：写对象参数 */
    public static final int WRITE_PARAMS = 0x08;
    /** 功能码：读告警数据 */
    public static final int READ_ALERT = 0x09;
    /** 功能码：告警确认 */
    public static final int CONFIRM_ALERT = 0x0A;
    /** 功能码：用户自定义数据 */
    public static final int CUSTOM_DATA = 0x0F;
    /** 功能码：登录 */
    public static final int LOGON = 0x21;
    /** 功能码：登录退出 */
    public static final int LOGOFF = 0x22;
    /** 功能码：心跳检验 */
    public static final int HEART_BEAT = 0x24;
    /** 功能码：发送短信 */
    public static final int SEND_SMS = 0x28;
    /** 功能码：收到短信上报 */
    public static final int RECEIVE_SMS = 0x29;
    /**功能码：历史日数据查询*/
    public static final int HISTORY_DATA = 0x0D;
    /** 功能码：终端控制 */
    public static final int RTU_CONTROL = 0x42;
    
    // 扩展功能码，用于通讯服务与前置机之间的通讯
    /** 功能码：读取终端状态 */
    public static final int READ_RTU_STATUS = 0x2A;
    /** 功能码：设置信道流量阀值/上报信道流量 */
    public static final int RTU_THROUGHPUT = 0x2B;
    
    // 自定义功能码，用于内部特殊用途，如将消息解码成原始报文或非法报文
    /** 功能码：解码为原始报文 */
    public static final int DECODE_RAW_DATA = 0xFA;
    /** 功能码：解码为非法报文 */
    public static final int DECODE_ILLEGAL_DATA = 0xFB;
    /** 功能码：序列化消息 */
    public static final int SERIALIZE_MESSAGE = 0xFC;
    /** 功能码：补召漏点数据 */
    public static final int REREAD_MISSING_DATA = 0xFD;
    /** 功能码：刷新通讯服务缓存 */
    public static final int REFRESH_CACHE = 0xFE;
    /** 功能码：其它自定义操作 */
    public static final int OTHER = 0xFF;
    
}