package com.hzjbbis.fk.model;

import java.util.ArrayList;
import java.util.List;

import com.hzjbbis.fk.utils.HexDump;

/**
 * �ն�����
 * @author ������
 */
public class TaskTemplate {

    /** ��������: ��ͨ���� */
    public static final String TASK_TYPE_NORMAL = "01";
    /** ��������: �м����� */
    public static final String TASK_TYPE_FORWARD = "02";   
    /** ���������ַ����и����Եķָ��� */
    private static final String DELIM = ",";
    
    /** ����ģ��ID */
    private String taskTemplateID;
    /** �������� */
    private String taskType;	
    /** ������ʼ��׼ʱ�� */
    private int sampleStartTime;
    /** ������ʼ��׼ʱ�䵥λ */
    private String sampleStartTimeUnit;
    /** �������ʱ�� */
    private int sampleInterval;
    /** �������ʱ�䵥λ */
    private String sampleIntervalUnit;
    /** ���ͻ�׼ʱ�� */
    private int uploadStartTime;
    /** ���ͻ�׼ʱ�䵥λ */
    private String uploadStartTimeUnit;
    /** ���ͼ��ʱ�� */
    private int uploadInterval;
    /** ���ͼ��ʱ�䵥λ */
    private String uploadIntervalUnit;
    /** �ϱ�����Ƶ�� */
    private int frequence;
    /**�������*/
    private int savepts;
    /**ִ�д���*/
    private int donums;			
    /** ����¼�����������ַ������ */
    private String dataCodesStr;
    /** ����¼����������б�[String] */
    private List<String> dataCodes=new ArrayList<String>();
    
    
    /** ��λ���� ���ڰ����б�������(�ǳ�ʼ������)*/
    //private String deptCode;
    /** ��������,�������ֲ�ͬ�����(�ǳ�ʼ������) */
    //private String taskPlateProperty;
	/** �������(�ǳ�ʼ������) */
    private String tn;
    /** ct(�ǳ�ʼ������) */
    //private int ct;
    /** pt(�ǳ�ʼ������) */
    //private int pt;
    /** ���ݱ���ID(�ǳ�ʼ������) */
    //private String dataSaveID;
                   
    /**
     * �����������ַ����н������ն�����������������ַ�����ʽ���壨�μ��㽭��Լ�����£�
     * ��ͨ����TT,TS.NN,TS.UU,TI.NN,TI.UU,RS.NN,RS.UU,RI.NN,RI.UU,RDI,TN,SP,RT,DIN,DI0,DI1,...
     * �м�����TT,TS.NN,TS.UU,TI.NN,TI.UU,RS.NN,RS.UU,RI.NN,RI.UU,RDI,PN,PS,SP,WT,CC,GF,GL,CL,CI
     * �쳣����TT,ALR,TI.NN,TI.UU,TN,DIN,DI0.TN,DI0.DI,...,RT
     * @param s ���������ַ���
     * @return �ն��������
     */
    public static TaskTemplate parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("The task setting could not be null");
        }
        
        String[] values = s.split(DELIM);
        String tt = values[0];
        if (!tt.equals(TASK_TYPE_NORMAL)
                && !tt.equals(TASK_TYPE_FORWARD)) {
            throw new IllegalArgumentException("Invalid task type: " + tt);
        }
        
        TaskTemplate task = new TaskTemplate();
        task.setTaskType(tt);
        if (tt.equals(TASK_TYPE_NORMAL)) {
            setTaskSchedule(task, values);
            task.setTn(values[10]);
            int diCount = Integer.parseInt(values[13]);
            for (int i = 0; i < diCount; i++) {
                task.addDataCode(values[14 + i]);
            }
        }
        else if (tt.equals(TASK_TYPE_FORWARD)) {
            setTaskSchedule(task, values);
        }                
        return task;
    }
    
    /**
     * ���������ִ�мƻ�
     * @param task �������
     * @param values ����ֵ����
     */
    private static void setTaskSchedule(TaskTemplate task, String[] values) {
        task.setSampleStartTime(Integer.parseInt(values[1]));
        task.setSampleStartTimeUnit(values[2]);
        task.setSampleInterval(Integer.parseInt(values[3]));
        task.setSampleIntervalUnit(values[4]);
        task.setUploadStartTime(Integer.parseInt(values[5]));
        task.setUploadStartTimeUnit(values[6]);
        task.setUploadInterval(Integer.parseInt(values[7]));
        task.setUploadIntervalUnit(values[8]);
        task.setFrequence(Integer.parseInt(values[9]));
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        return sb.toString();
    }
    
    /**
     * ������������
     * @param code �ϱ����������
     */
    public void addDataCode(String code) {  
        dataCodes.add(code);
    }
    
    /**
     * ȡ������¼�����������ַ������
     * @return
     */
    public String getDataCodesAsString() {
        if (dataCodesStr == null && dataCodes != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dataCodes.size(); i++) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append((String) dataCodes.get(i));
            }
        }
        return dataCodesStr;
    }
    
    
    /**
     * @return Returns the taskType.
     */
    public String getTaskType() {
        return taskType;
    }
    /**
     * @param taskType The taskType to set.
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    /**
     * @return Returns the tn.
     */
    public String getTn() {
        return tn;
    }
    /**
     * @param tn The tn to set.
     */
    public void setTn(String tn) {
        this.tn = tn;
    }   
    
    /**
     * @return Returns the sampleStartTime.
     */
    public int getSampleStartTime() {
        return sampleStartTime;
    }
    /**
     * @param sampleStartTime The sampleStartTime to set.
     */
    public void setSampleStartTime(int sampleStartTime) {
        this.sampleStartTime = sampleStartTime;
    }
    /**
     * @return Returns the sampleStartTimeUnit.
     */
    public String getSampleStartTimeUnit() {
        return sampleStartTimeUnit;
    }
    /**
     * @param sampleStartTimeUnit The sampleStartTimeUnit to set.
     */
    public void setSampleStartTimeUnit(String sampleStartTimeUnit) {
        this.sampleStartTimeUnit = sampleStartTimeUnit;
    }
    /**
     * @return Returns the sampleInterval.
     */
    public int getSampleInterval() {
        return sampleInterval;
    }
    /**
     * @param sampleInterval The sampleInterval to set.
     */
    public void setSampleInterval(int sampleInterval) {
        this.sampleInterval = sampleInterval;
    }
    /**
     * @return Returns the sampleIntervalUnit.
     */
    public String getSampleIntervalUnit() {
        return sampleIntervalUnit;
    }
    /**
     * @param sampleIntervalUnit The sampleIntervalUnit to set.
     */
    public void setSampleIntervalUnit(String sampleIntervalUnit) {
        this.sampleIntervalUnit = sampleIntervalUnit;
    }
    /**
     * @return Returns the uploadStartTime.
     */
    public int getUploadStartTime() {
        return uploadStartTime;
    }
    /**
     * @param uploadStartTime The uploadStartTime to set.
     */
    public void setUploadStartTime(int uploadStartTime) {
        this.uploadStartTime = uploadStartTime;
    }
    /**
     * @return Returns the uploadStartTimeUnit.
     */
    public String getUploadStartTimeUnit() {
        return uploadStartTimeUnit;
    }
    /**
     * @param uploadStartTimeUnit The uploadStartTimeUnit to set.
     */
    public void setUploadStartTimeUnit(String uploadStartTimeUnit) {
        this.uploadStartTimeUnit = uploadStartTimeUnit;
    }
    /**
     * @return Returns the uploadInterval.
     */
    public int getUploadInterval() {
        return uploadInterval;
    }
    /**
     * @param uploadInterval The uploadInterval to set.
     */
    public void setUploadInterval(int uploadInterval) {
        this.uploadInterval = uploadInterval;
    }
    /**
     * @return Returns the uploadIntervalUnit.
     */
    public String getUploadIntervalUnit() {
        return uploadIntervalUnit;
    }
    /**
     * @param uploadIntervalUnit The uploadIntervalUnit to set.
     */
    public void setUploadIntervalUnit(String uploadIntervalUnit) {
        this.uploadIntervalUnit = uploadIntervalUnit;
    }
    /**
     * @return Returns the frequence.
     */
    public int getFrequence() {
        return frequence;
    }
    /**
     * @param frequence The frequence to set.
     */
    public void setFrequence(int frequence) {
        this.frequence = frequence;
    }
    
    /**
     * @return Returns the dataCodes.
     */
    public List<String> getDataCodes() {
        return dataCodes;
    }
    /**
     * @param dataCodes The dataCodes to set.
     */
    public void setDataCodes(List<String> dataCodes) {
        this.dataCodes = dataCodes;
        this.dataCodesStr = null;
    }
   

	

	public int getDonums() {
		return donums;
	}

	public int getSavepts() {
		return savepts;
	}

	
	public void setDonums(int donums) {
		this.donums = donums;
	}

	public void setSavepts(int savepts) {
		this.savepts = savepts;
	}
    
  

	public String toCodeValue(){
    	String desc=null;
    	try{
    		StringBuffer sb=new StringBuffer();
    		if(taskType.equalsIgnoreCase(TASK_TYPE_NORMAL)){
    			sb.append(taskType);
    			sb.append(",");
    			sb.append(sampleStartTimeUnit);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)sampleStartTime));
    			sb.append(",");
    			sb.append(sampleIntervalUnit);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)sampleInterval));
    			sb.append(",");
    			sb.append(uploadStartTimeUnit);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)uploadStartTime));
    			sb.append(",");
    			sb.append(uploadIntervalUnit);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)uploadInterval));
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)frequence));
    			sb.append(",");
    			sb.append(tn);
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)savepts));
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)donums));
    			sb.append(",");
    			sb.append(HexDump.toHex((byte)dataCodes.size()));
    			sb.append(",");
    			sb.append(getDataCodesAsString());
    		}
    		desc=sb.toString();
    	}catch(Exception e){
    	}
    	return desc;
    }

	/**
	 * @return ���� dataCodesStr��
	 */
	public String getDataCodesStr() {
		return dataCodesStr;
	}

	/**
	 * @param dataCodesStr Ҫ���õ� dataCodesStr��
	 */
	public void setDataCodesStr(String dataCodesStr) {
		this.dataCodesStr = dataCodesStr;
	}

	public String getTaskTemplateID() {
		return taskTemplateID;
	}

	public void setTaskTemplateID(String taskTemplateID) {
		this.taskTemplateID = taskTemplateID;
	}	

}
