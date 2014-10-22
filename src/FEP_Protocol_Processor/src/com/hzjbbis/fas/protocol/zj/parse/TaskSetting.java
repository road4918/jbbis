package com.hzjbbis.fas.protocol.zj.parse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;

import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.model.TaskTemplate;

public class TaskSetting {
	private final Log log=LogFactory.getLog(TaskSetting.class);
	public static final int TIME_UNIT_MINUTE=0x02;
	public static final int TIME_UNIT_HOUR=0x03;
	public static final int TIME_UNIT_DAY=0x04;
	public static final int TIME_UNIT_MONTH=0x05;
	
	private int TT;		/*��������*/
	private int TS; 	/*������ʼ��׼ʱ��ֵ*/
	private int TSUnit; /*������ʼ��׼ʱ�䵥λ*/
	private int TI; 	/*�������ʱ��ֵ*/
	private int TIUnit; /*�������ʱ�䵥λ*/
	private int RS; 	/*���ͻ�׼ʱ��ֵ*/
	private int RSUnit; /*���ͻ�׼ʱ�䵥λ*/
	private int RI; 	/*���ͼ��ʱ��ֵ*/
	private int RIUnit; /*���ͼ��ʱ�䵥λ*/
	private int RDI; 	/*�������ݵ�Ƶ��*/
	private int TN; 	/*�������*/
	private int SP; 	/*���񱣴����*/
	private int RT; 	/*����ִ�д���*/
	private int DIN; 	/*���������*/
	private List DI; 	/*������*/
	
	/** ��λ���� �������񱣴�*/
    //private String deptCode;
	/** ���ݱ���ID �������񱣴�*/
    //private String dataSaveID;
    /** �ն��������� �������񱣴�*/
    //private String taskProperty;
    /** ct �������񱣴�*/
    //private int ct;
    /** pt �������񱣴�*/
    //private int pt;
	private BizRtu rtu=null;
	private TaskTemplate rtask=null;
	
	public TaskSetting(){
		
	}
	
	/**
	 * 
	 * @param rtuid
	 * @param taskid
	 */
	public TaskSetting(int rtua,int taskid,ProtocolDataConfig pdc){
		//just for debug
		TT=0;
		TS=0;
		TSUnit=TIME_UNIT_MINUTE;
		TI=15;
		TIUnit=TIME_UNIT_MINUTE;
		RS=5;
		RSUnit=TIME_UNIT_MINUTE;
		RI=1;
		RIUnit=TIME_UNIT_DAY;
		RDI=1;
		TN=0;
		SP=96;
		RT=0;
		DIN=1;
		DI=new ArrayList();		
		try{
			this.rtu=RtuManage.getInstance().getBizRtuInCache(rtua);
			if (this.rtu==null)
				return;
			this.rtask=rtu.getTaskTemplate(String.valueOf(taskid));
			if (this.rtask==null)
				return;
			List dids=rtask.getDataCodes();
			log.info("�������ã��ն�--"+ParseTool.IntToHex4(rtua)+
					"�������--"+taskid+"�����������--"+dids.size());
			for(int iter=0;iter<dids.size();iter++){
				DI.add(iter,pdc.getDataItemConfig((String)dids.get(iter)));
			}
			TS=rtask.getSampleStartTime();
			TSUnit=Integer.parseInt(rtask.getSampleStartTimeUnit(),16);
			TI=rtask.getSampleInterval();
			TIUnit=Integer.parseInt(rtask.getSampleIntervalUnit(),16);
			RS=rtask.getUploadStartTime();
			RSUnit=Integer.parseInt(rtask.getUploadStartTimeUnit(),16);
			RI=rtask.getUploadInterval();
			RIUnit=Integer.parseInt(rtask.getUploadIntervalUnit(),16);
			RDI=rtask.getFrequence();
			
		}catch(Exception e){
			throw new MessageDecodeException("�޷���ȡ�ն��������ã��ն��߼���ַ��"+ParseTool.IntToHex4(rtua)+"������ţ�"+taskid);
		}
	}
	
	/**
	 * ȡһ�����������ܳ���
	 * @return
	 */
	public int getDataLength(){
		int rt=0;
		for(Iterator iter=DI.iterator();iter.hasNext();){
			ProtocolDataItemConfig dc=(ProtocolDataItemConfig)iter.next();
			rt+=dc.getLength();
		}
		return rt;
	}
	
	public String getDataCodes(){
		StringBuffer sb=new StringBuffer();
		for(Iterator iter=DI.iterator();iter.hasNext();){
			ProtocolDataItemConfig dc=(ProtocolDataItemConfig)iter.next();
			if(sb.length()>0){
				sb.append(",");
			}
			sb.append(dc.getCode());
		}
		return sb.toString();
	}
	
	public int getDataNum(){
		int rt=0;
		if(DI!=null){
			rt=DI.size();
		}
		return rt;
	}
	
	/**
	 * @return Returns the dI.
	 */
	public List getDI() {
		return DI;
	}

	/**
	 * @param di The dI to set.
	 */
	public void setDI(List di) {
		DI = di;
	}

	/**
	 * @return Returns the dIN.
	 */
	public int getDIN() {
		return DIN;
	}

	/**
	 * @param din The dIN to set.
	 */
	public void setDIN(int din) {
		DIN = din;
	}

	/**
	 * @return Returns the rDI.
	 */
	public int getRDI() {
		return RDI;
	}

	/**
	 * @param rdi The rDI to set.
	 */
	public void setRDI(int rdi) {
		RDI = rdi;
	}

	/**
	 * @return Returns the rI.
	 */
	public int getRI() {
		return RI;
	}

	/**
	 * @param ri The rI to set.
	 */
	public void setRI(int ri) {
		RI = ri;
	}

	/**
	 * @return Returns the rIUnit.
	 */
	public int getRIUnit() {
		return RIUnit;
	}

	/**
	 * @param unit The rIUnit to set.
	 */
	public void setRIUnit(int unit) {
		RIUnit = unit;
	}

	/**
	 * @return Returns the rS.
	 */
	public int getRS() {
		return RS;
	}

	/**
	 * @param rs The rS to set.
	 */
	public void setRS(int rs) {
		RS = rs;
	}

	/**
	 * @return Returns the rSUnit.
	 */
	public int getRSUnit() {
		return RSUnit;
	}

	/**
	 * @param unit The rSUnit to set.
	 */
	public void setRSUnit(int unit) {
		RSUnit = unit;
	}

	/**
	 * @return Returns the rT.
	 */
	public int getRT() {
		return RT;
	}

	/**
	 * @param rt The rT to set.
	 */
	public void setRT(int rt) {
		RT = rt;
	}

	/**
	 * @return Returns the sP.
	 */
	public int getSP() {
		return SP;
	}

	/**
	 * @param sp The sP to set.
	 */
	public void setSP(int sp) {
		SP = sp;
	}

	/**
	 * @return Returns the tI.
	 */
	public int getTI() {
		return TI;
	}

	/**
	 * @param ti The tI to set.
	 */
	public void setTI(int ti) {
		TI = ti;
	}

	/**
	 * @return Returns the tIUnit.
	 */
	public int getTIUnit() {
		return TIUnit;
	}

	/**
	 * @param unit The tIUnit to set.
	 */
	public void setTIUnit(int unit) {
		TIUnit = unit;
	}

	/**
	 * @return Returns the tN.
	 */
	public int getTN() {
		return TN;
	}

	/**
	 * @param tn The tN to set.
	 */
	public void setTN(int tn) {
		TN = tn;
	}

	/**
	 * @return Returns the tS.
	 */
	public int getTS() {
		return TS;
	}

	/**
	 * @param ts The tS to set.
	 */
	public void setTS(int ts) {
		TS = ts;
	}

	/**
	 * @return Returns the tSUnit.
	 */
	public int getTSUnit() {
		return TSUnit;
	}

	/**
	 * @param unit The tSUnit to set.
	 */
	public void setTSUnit(int unit) {
		TSUnit = unit;
	}

	/**
	 * @return Returns the tT.
	 */
	public int getTT() {
		return TT;
	}

	/**
	 * @param tt The tT to set.
	 */
	public void setTT(int tt) {
		TT = tt;
	}

	
	

	/**
	 * @return Returns the rtu.
	 */
	public BizRtu getRtu() {
		return rtu;
	}

	public TaskTemplate getRtask() {
		return rtask;
	}

	public void setRtask(TaskTemplate rtask) {
		this.rtask = rtask;
	}

	
}
