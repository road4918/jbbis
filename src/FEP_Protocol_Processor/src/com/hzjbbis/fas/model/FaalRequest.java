/*
 * Created on 2006-2-27
 */
package com.hzjbbis.fas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hzjbbis.fas.protocol.zj.FunctionCode;


/**
 * FAAL 通讯请求
 * @author 张文亮
 */
public abstract class FaalRequest implements Serializable {
    private static final long serialVersionUID = 2937756926363569712L;
    
    /** 请求类型：读中继 */
    public static final int TYPE_READ_FORWARD_DATA = FunctionCode.READ_FORWARD_DATA;
    /** 请求类型：读当前数据 */
    public static final int TYPE_READ_CURRENT_DATA = FunctionCode.READ_CURRENT_DATA;
    /** 请求类型：读任务数据 */
    public static final int TYPE_READ_TASK_DATA = FunctionCode.READ_TASK_DATA;
    /** 请求类型：读编程日志 */
    public static final int TYPE_READ_PROGRAM_LOG = FunctionCode.READ_PROGRAM_LOG;
    /** 请求类型：实时写对象参数 */
    public static final int TYPE_REALTIME_WRITE_PARAMS = FunctionCode.REALTIME_WRITE_PARAMS;
    /** 请求类型：写对象参数 */
    public static final int TYPE_WRITE_PARAMS = FunctionCode.WRITE_PARAMS;
    /** 请求类型：读告警数据 */
    public static final int TYPE_READ_ALERT = FunctionCode.READ_ALERT;
    /** 请求类型：告警确认 */
    public static final int TYPE_CONFIRM_ALERT = FunctionCode.CONFIRM_ALERT;
    /** 请求类型：用户自定义数据 */
    public static final int TYPE_CUSTOM_DATA = FunctionCode.CUSTOM_DATA;
    /** 请求类型：登录 */
    public static final int TYPE_LOGON = FunctionCode.LOGON;
    /** 请求类型：登录退出 */
    public static final int TYPE_LOGOFF = FunctionCode.LOGOFF;
    /** 请求类型：心跳检验 */
    public static final int TYPE_HEART_BEAT = FunctionCode.HEART_BEAT;
    /** 请求类型：发送短信 */
    public static final int TYPE_SEND_SMS = FunctionCode.SEND_SMS;
    /** 请求类型：收到短信上报 */
    public static final int TYPE_RECEIVE_SMS = FunctionCode.RECEIVE_SMS;
    /** 请求类型：终端控制 */
    public static final int TYPE_RTU_CONTROL = FunctionCode.RECEIVE_SMS;
    /**请求类型：历史日数据查询*/
    public static final int TYPE_HISTORY_DATA= FunctionCode.HISTORY_DATA;
    /**请求类型：终端参数查询*/
    public static final int TYPE_READ_RTUPARAM= 0x09;
    /** 自定义请求类型：补召漏点数据 */
    public static final int TYPE_REREAD_MISSING_DATA = FunctionCode.REREAD_MISSING_DATA;
    /** 自定义请求类型：刷新通讯服务缓存 */
    public static final int TYPE_REFRESH_CACHE = FunctionCode.REFRESH_CACHE;
    /** 自定义请求类型：其它操作 */
    public static final int TYPE_OTHER = FunctionCode.OTHER;
    
    /**表通讯参数设置*/
    public static final int HGOM_METER_PARAMETER_SET = 0xA1;
    /**表通讯参数查询*/
    public static final int HGOM_METER_PARAMETER_QUERY = 0xA2;
    /**过路读数据*/
    public static final int HGOM_PAST_DATA_READ = 0xA3;
    /**过路写数据*/
    public static final int HGOM_PAST_DATA_WRITE = 0xA4;
    /**冻结数据*/
    public static final int HGOM_FREEZE_DATA = 0xAA;
    /**冻结数据查询*/
    public static final int HGOM_FREEZE_DATA_QUERY = 0xAB;
    /**冻结抄表数据*/
    public static final int HGOM_FREEZE_METER_DATA = 0xAD;
    /**查询冻结抄表数据*/
    public static final int HGOM_FREEZE_METER_DATA_QUERY = 0xAE;
    /**查询抄表日数据*/
    public static final int HGOM_METER_DAY_DATA_QUERY = 0xAF;
    /**间接读抄表数据*/
    public static final int HGOM_METER_DATA_INDERECT_QUERY = 0xAC;
    /**设置终端自动冻结表数据时间*/
    public static final int HGOM_METER_DATA_FREEZING_TIME =0xB3;
    /**查询终端自动冻结表数据时间*/
    public static final int HGOM_METER_DATA_FREEZING_TIME_QUERY =0xB4;
    /**查询表的断相数据*/
    public static final int HGOM_METER_LACK_DATA_QUERY = 0xB1;
    /**查询表的事故告警信息数据*/
    public static final int HGOM_METER_WARN_ACCIDENT_DATA_QUERY = 0xB2;
    /**查询表24点抄表数据*/
    public static final int HGOM_METER_DATA_24_QUERY = 0xB6;
    /**24点电压*/
    public static final int HGOM_VOLTAGE_POINT_24 = 0xB7;
    /**96点电压*/
    public static final int HGOM_VOLTAGE_POINT_96 = 0xB8;
    /**表96点电量*/
    public static final int HGOM_ELEC_QUANTITY_POINT_96 = 0xB9;
    /**24点电流*/
    public static final int HGOM_ELEC_POINT_24 = 0xBA;
    /**96点电流*/
    public static final int HGOM_ELEC_POINT_96 = 0xBB;
    /**失压记录*/
    public static final int HGOM_VOLTAGE_LOST_RECORD = 0xBE;
    
    
    
    public static final int HGCS_QUERY_DATA=0x57;
    public static final int HGCS_LOAD_SETTING=0x55;
    public static final int HGCS_QUERY_FREEZE=0x54;
    public static final int HGCS_SETTING_FREEZE=0x53;
    
    public static final int TEST=0x01;
    
    /** 规约类型 */
    protected String protocol;
    /** 命令类型 */
    protected int type;

    /** 通讯请求发起人 */
    private String operator;
    /** 请求参数列表 */
    private List<FaalRequestParam> params;
    /** 终端ID列表 */
    private List<String> rtuIds;
    /** 主站操作命令ID列表。与终端ID一一对应 */
    private List<Long> cmdIds;
    /** 是否在以后发送该通讯请求 */
    private boolean scheduled = false;
    /** 计划的发送时间 */
    private Date scheduledTime;
    
    private boolean taskflag=false;
    private long timetag;
    /**
     * 添加请求参数
     * @param param 请求参数
     */
    public void addParam(FaalRequestParam param) {
        if (params == null) {
            params = new ArrayList<FaalRequestParam>();
        }
        params.add(param);
    }
    
    /**
     * 添加请求参数
     * @param name 参数名称
     * @param value 参数值
     */
    public void addParam(String name, String value) {
        addParam(new FaalRequestParam(name, value));
    }
    
    /**
     * 设置终端ID
     * @param ids 终端ID数组
     */
    public void setRtuIds(String[] ids) {
        this.rtuIds = Arrays.asList(ids);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(", type=").append(type)
            .append(", rtuCount=").append(rtuIds == null ? 0 : rtuIds.size())
            .append(", rtuIds=").append(rtuIds)
            .append(", cmdIds=").append(cmdIds).append("]");
        
        return sb.toString();
    }
    
    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }
    /**
     * @return Returns the protocol.
     */
    public String getProtocol() {
        return protocol;
    }
    /**
     * @param protocol The protocol to set.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    /**
     * @return Returns the operation.
     */

    /**
     * @return Returns the operator.
     */
    public String getOperator() {
        return operator;
    }
    /**
     * @param operator The operator to set.
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }
    /**
     * @return Returns the params.
     */
    public List<FaalRequestParam> getParams() {
        return params;
    }
    /**
     * @param params The params to set.
     */
    public void setParams(List<FaalRequestParam> params) {
        this.params = params;
    }
    /**
     * @return Returns the rtuIds.
     */
    public List<String> getRtuIds() {
        return rtuIds;
    }
    /**
     * @param rtuIds The rtuIds to set.
     */
    public void setRtuIds(List<String> rtuIds) {
        this.rtuIds = rtuIds;
    }
    /**
     * @return Returns the cmdIds.
     */
    public List<Long> getCmdIds() {
        return cmdIds;
    }
    /**
     * @param cmdIds The cmdIds to set.
     */
    public void setCmdIds(List<Long> cmdIds) {
        this.cmdIds = cmdIds;
    }
    /**
     * @return Returns the scheduled.
     */
    public boolean isScheduled() {
        return scheduled;
    }
    /**
     * @param scheduled The scheduled to set.
     */
    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }
    /**
     * @return Returns the scheduledTime.
     */
    public Date getScheduledTime() {
        return scheduledTime;
    }
    /**
     * @param scheduledTime The scheduledTime to set.
     */
    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

	public boolean isTaskflag() {
		return taskflag;
	}

	public void setTaskflag(boolean taskflag) {
		this.taskflag = taskflag;
	}

	public long getTimetag() {
		return timetag;
	}

	public void setTimetag(long timetag) {
		this.timetag = timetag;
	}
    
    
}