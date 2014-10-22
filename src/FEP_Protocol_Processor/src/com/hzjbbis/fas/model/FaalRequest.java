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
 * FAAL ͨѶ����
 * @author ������
 */
public abstract class FaalRequest implements Serializable {
    private static final long serialVersionUID = 2937756926363569712L;
    
    /** �������ͣ����м� */
    public static final int TYPE_READ_FORWARD_DATA = FunctionCode.READ_FORWARD_DATA;
    /** �������ͣ�����ǰ���� */
    public static final int TYPE_READ_CURRENT_DATA = FunctionCode.READ_CURRENT_DATA;
    /** �������ͣ����������� */
    public static final int TYPE_READ_TASK_DATA = FunctionCode.READ_TASK_DATA;
    /** �������ͣ��������־ */
    public static final int TYPE_READ_PROGRAM_LOG = FunctionCode.READ_PROGRAM_LOG;
    /** �������ͣ�ʵʱд������� */
    public static final int TYPE_REALTIME_WRITE_PARAMS = FunctionCode.REALTIME_WRITE_PARAMS;
    /** �������ͣ�д������� */
    public static final int TYPE_WRITE_PARAMS = FunctionCode.WRITE_PARAMS;
    /** �������ͣ����澯���� */
    public static final int TYPE_READ_ALERT = FunctionCode.READ_ALERT;
    /** �������ͣ��澯ȷ�� */
    public static final int TYPE_CONFIRM_ALERT = FunctionCode.CONFIRM_ALERT;
    /** �������ͣ��û��Զ������� */
    public static final int TYPE_CUSTOM_DATA = FunctionCode.CUSTOM_DATA;
    /** �������ͣ���¼ */
    public static final int TYPE_LOGON = FunctionCode.LOGON;
    /** �������ͣ���¼�˳� */
    public static final int TYPE_LOGOFF = FunctionCode.LOGOFF;
    /** �������ͣ��������� */
    public static final int TYPE_HEART_BEAT = FunctionCode.HEART_BEAT;
    /** �������ͣ����Ͷ��� */
    public static final int TYPE_SEND_SMS = FunctionCode.SEND_SMS;
    /** �������ͣ��յ������ϱ� */
    public static final int TYPE_RECEIVE_SMS = FunctionCode.RECEIVE_SMS;
    /** �������ͣ��ն˿��� */
    public static final int TYPE_RTU_CONTROL = FunctionCode.RECEIVE_SMS;
    /**�������ͣ���ʷ�����ݲ�ѯ*/
    public static final int TYPE_HISTORY_DATA= FunctionCode.HISTORY_DATA;
    /**�������ͣ��ն˲�����ѯ*/
    public static final int TYPE_READ_RTUPARAM= 0x09;
    /** �Զ����������ͣ�����©������ */
    public static final int TYPE_REREAD_MISSING_DATA = FunctionCode.REREAD_MISSING_DATA;
    /** �Զ����������ͣ�ˢ��ͨѶ���񻺴� */
    public static final int TYPE_REFRESH_CACHE = FunctionCode.REFRESH_CACHE;
    /** �Զ����������ͣ��������� */
    public static final int TYPE_OTHER = FunctionCode.OTHER;
    
    /**��ͨѶ��������*/
    public static final int HGOM_METER_PARAMETER_SET = 0xA1;
    /**��ͨѶ������ѯ*/
    public static final int HGOM_METER_PARAMETER_QUERY = 0xA2;
    /**��·������*/
    public static final int HGOM_PAST_DATA_READ = 0xA3;
    /**��·д����*/
    public static final int HGOM_PAST_DATA_WRITE = 0xA4;
    /**��������*/
    public static final int HGOM_FREEZE_DATA = 0xAA;
    /**�������ݲ�ѯ*/
    public static final int HGOM_FREEZE_DATA_QUERY = 0xAB;
    /**���᳭������*/
    public static final int HGOM_FREEZE_METER_DATA = 0xAD;
    /**��ѯ���᳭������*/
    public static final int HGOM_FREEZE_METER_DATA_QUERY = 0xAE;
    /**��ѯ����������*/
    public static final int HGOM_METER_DAY_DATA_QUERY = 0xAF;
    /**��Ӷ���������*/
    public static final int HGOM_METER_DATA_INDERECT_QUERY = 0xAC;
    /**�����ն��Զ����������ʱ��*/
    public static final int HGOM_METER_DATA_FREEZING_TIME =0xB3;
    /**��ѯ�ն��Զ����������ʱ��*/
    public static final int HGOM_METER_DATA_FREEZING_TIME_QUERY =0xB4;
    /**��ѯ��Ķ�������*/
    public static final int HGOM_METER_LACK_DATA_QUERY = 0xB1;
    /**��ѯ����¹ʸ澯��Ϣ����*/
    public static final int HGOM_METER_WARN_ACCIDENT_DATA_QUERY = 0xB2;
    /**��ѯ��24�㳭������*/
    public static final int HGOM_METER_DATA_24_QUERY = 0xB6;
    /**24���ѹ*/
    public static final int HGOM_VOLTAGE_POINT_24 = 0xB7;
    /**96���ѹ*/
    public static final int HGOM_VOLTAGE_POINT_96 = 0xB8;
    /**��96�����*/
    public static final int HGOM_ELEC_QUANTITY_POINT_96 = 0xB9;
    /**24�����*/
    public static final int HGOM_ELEC_POINT_24 = 0xBA;
    /**96�����*/
    public static final int HGOM_ELEC_POINT_96 = 0xBB;
    /**ʧѹ��¼*/
    public static final int HGOM_VOLTAGE_LOST_RECORD = 0xBE;
    
    
    
    public static final int HGCS_QUERY_DATA=0x57;
    public static final int HGCS_LOAD_SETTING=0x55;
    public static final int HGCS_QUERY_FREEZE=0x54;
    public static final int HGCS_SETTING_FREEZE=0x53;
    
    public static final int TEST=0x01;
    
    /** ��Լ���� */
    protected String protocol;
    /** �������� */
    protected int type;

    /** ͨѶ�������� */
    private String operator;
    /** ��������б� */
    private List<FaalRequestParam> params;
    /** �ն�ID�б� */
    private List<String> rtuIds;
    /** ��վ��������ID�б����ն�IDһһ��Ӧ */
    private List<Long> cmdIds;
    /** �Ƿ����Ժ��͸�ͨѶ���� */
    private boolean scheduled = false;
    /** �ƻ��ķ���ʱ�� */
    private Date scheduledTime;
    
    private boolean taskflag=false;
    private long timetag;
    /**
     * ����������
     * @param param �������
     */
    public void addParam(FaalRequestParam param) {
        if (params == null) {
            params = new ArrayList<FaalRequestParam>();
        }
        params.add(param);
    }
    
    /**
     * ����������
     * @param name ��������
     * @param value ����ֵ
     */
    public void addParam(String name, String value) {
        addParam(new FaalRequestParam(name, value));
    }
    
    /**
     * �����ն�ID
     * @param ids �ն�ID����
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