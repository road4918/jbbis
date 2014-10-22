package com.hzjbbis.fk.bp.processor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.db.batch.event.adapt.BaseReadTaskHandler;
import com.hzjbbis.fas.framework.message.MessageZjpb;
import com.hzjbbis.fas.model.RtuData;
import com.hzjbbis.fas.model.RtuDataItem;
import com.hzjbbis.fas.protocol.Protocol;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.handler.ProtocolHandlerFactory;
import com.hzjbbis.fk.bp.model.MessageLogErr;
import com.hzjbbis.fk.bp.model.TaskDLSJ;
import com.hzjbbis.fk.bp.model.TaskDYHGLSJ;
import com.hzjbbis.fk.bp.model.TaskData;
import com.hzjbbis.fk.bp.model.TaskFHSJ;
import com.hzjbbis.fk.bp.model.TaskItemData;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.TaskDbConfig;
import com.hzjbbis.fk.model.TaskDbConfigItem;
import com.hzjbbis.fk.utils.HexDump;
import com.hzjbbis.util.ArraysUtil;

public class TaskMessageHandler extends BaseReadTaskHandler {
	private static final Logger log = Logger.getLogger(TaskMessageHandler.class);
	//�ϱ��������ݱ���
	private static final String TABLE_NAME_DLSJ="TEMP_SB_DLSJ";
	//�ϱ��������ݱ���
	private static final String TABLE_NAME_FHSJ="TEMP_SB_FHSJ";
	//�ϱ���ѹ�ϸ������ݱ���
	private static final String TABLE_NAME_DLHGLSJ="TEMP_SB_DYHGLSJ";
	//���������Ե����ݱ���
    private String[] taskCodes=new String[]{"8E62","B630"};
	
	public void handleReadTask(AsyncService service,MessageZj msg){
		try {  
			//�ֱ��ն˹�Լ:�㽭��Լ���㽭����Լ
			BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(msg.head.rtua));
			if (rtu==null){
				log.error("not find rtu in cache:"+HexDump.toHex(msg.head.rtua));
			}
			Class messageType = MessageZj.class;;                    
	    	if (msg instanceof MessageZj){            			    			    		
	    		if (rtu!=null&&rtu.getRtuProtocol().equals(Protocol.ZJPB)){
	        		messageType = MessageZjpb.class;                  		              	
	        	}             	
	    	} 
	    	//���ù�Լ��������
	    	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
	        ProtocolHandler handler = factory.getProtocolHandler(messageType);                	
	        Object value = handler.process(msg);  
	        if (value!=null){
	        	int bqbj=0;
	        	if (msg.head.msta!=0)//��վ��ַ�����жϱ����Ƿ�Ϊ��������:0�ն���������;��0�ٲⷵ��
	        		bqbj=1;
	        	taskDbConfigAnalyze(rtu,service,(List<RtuData>)value,bqbj,msg);
	        }
		}catch (Exception ex) {//�������ʧ����Ҫ����Ƿ�����֡����			
			saveErrMsgToDb(msg);
            log.error("Error to processing task message:"+msg, ex);
        }            
	}
	
	public void saveErrMsgToDb(MessageZj msg){
		try{
			MessageLogErr msgLogErr=new MessageLogErr();
			msgLogErr.setLogicAddress(HexDump.toHex(msg.head.rtua));
			msgLogErr.setQym(msgLogErr.getLogicAddress().substring(0,2));
			msgLogErr.setKzm(Integer.toString(msg.head.c_func, 16));			
			msgLogErr.setTime(new Date(msg.getIoTime()));
			msgLogErr.setBody(msg.getRawPacketString());					
			service.addToDao(msgLogErr,Integer.parseInt("5002"));
		}catch(Exception ex){
			log.error("save ErrMsg To Db error:"+ex);
		}
	}
	/*
	 * ������������RtuData���и����������Էֱ𱣴浽�����̱�;
	 * ��������Ҫ��ÿ������������Ӧ�����ݿ��ӳ����������,
	 * ��ͬ���������γɶ���򵥸����ݿ��ӳ�����,��11���б���01~11
	 * 99��ʾ�̱�����key=1000,������Ϊ����key={1001~1011,2001~2011,3001~3011}.
	 * */
	public void taskDbConfigAnalyze(BizRtu rtu,AsyncService service,List<RtuData> rtuDatas,int bqbj,MessageZj msg){
		try{
			for (int i = 0; i < rtuDatas.size(); i++) {
				String deptCode=rtu.getDeptCode();
				if (deptCode.length()==4)//��λ���������4λ,ǰ��λ��Ϊ�ֵ��б����key
					deptCode=deptCode.substring(0,2);
				else{
					log.error("�ն�"+rtu.getLogicAddress()+"�ĵ�λ����"+deptCode+"�Ƿ�");
					saveErrMsgToDb(msg);
					continue;
				}
				RtuData data= (RtuData) rtuDatas.get(i);
				RtuTask rt=rtu.getRtuTask(data.getTaskNum());
				if (rt==null){
					log.error("�ն�"+rtu.getLogicAddress()+"������"+data.getTaskNum()+"������");
					saveErrMsgToDb(msg);
					continue;
				}
				MeasuredPoint mp=rtu.getMeasuredPoint(rt.getTn());
				if (mp==null){
					log.error("�ն�"+rtu.getLogicAddress()+"�Ĳ������"+rt.getTn()+"������");
					saveErrMsgToDb(msg);
					continue;
				}				
				TaskDLSJ taskSBDL=null;
				TaskFHSJ taskFHSJ=null;
				TaskDYHGLSJ taskDLHGLSJ=null;
				for (int j = 0; j < data.getDataList().size(); j++) {
					RtuDataItem dataItem=(RtuDataItem)data.getDataList().get(j);
					//�������
					if (ArraysUtil.contains(taskCodes,dataItem.getCode())) {
						TaskItemData taskItemData=new TaskItemData();
						taskItemData.setDeptCode(rtu.getDeptCode());
						taskItemData.setCustomerNo(rtu.getMeasuredPoint(mp.getTn()).getCustomerNo());
						taskItemData.setStationNo(rtu.getMeasuredPoint(mp.getTn()).getStationNo());													
						taskItemData.setRtuId(rtu.getRtuId());
						taskItemData.setCode(dataItem.getCode());
						taskItemData.setValue(dataItem.getValue());
						taskItemData.setTxfs(msg.getTxfs());
						taskItemData.setCt(mp.getCt());
						taskItemData.setPt(mp.getPt());
						taskItemData.setTime(data.getTime());
						BPLatterProcessor.getInstance().rtuDataAdd(taskItemData);
			        }
					if (rt.getTaskTemplateProperty().equals("99")){//�����������ͱ���̱�
						TaskData taskData=new TaskData();
						taskData.setSJID(mp.getDataSaveID());
						taskData.setSJSJ(data.getTime());
						taskData.setBQBJ(bqbj);
						taskData.setCT(mp.getCt());
						taskData.setPT(mp.getPt());
						taskData.setSJBH(dataItem.getCode());
						taskData.setSJZ(dataItem.getValue());
						service.addToDao(taskData,Integer.parseInt("1000"));
					}
					else{//���泤��,�㷨:��ÿ������������Ӧ�����ݿ��ӳ����������
						TaskDbConfig taskDbConfig=RtuManage.getInstance().getTaskDbConfigInCache(dataItem.getCode());
						if (taskDbConfig==null){
							if (log.isInfoEnabled())
								log.info("sjbm="+dataItem.getCode()+"not find in getSjxMap");
							continue;
						}
						for (int k = 0; k < taskDbConfig.getTaskDbConfigItemList().size(); k++) {
							TaskDbConfigItem taskDbConfigItem=(TaskDbConfigItem)taskDbConfig.getTaskDbConfigItemList().get(k);
							//��������ƥ�䣬����ģ�����Բ��ڵ�ǰ�������������б��򲻱���
							if (!taskDbConfigItem.taskPropertyContains(rt.getTaskTemplateProperty())){
								continue;
							}
							//������Ҫ�����ж�TaskDbConfigItem.tag,"00":��ʾֻ��������ݲű���;"01"��ʾֻ���������ݲű���
							if (taskDbConfigItem.getTag().equals("00")||taskDbConfigItem.getTag().equals("01")){								
								if (!saveJudgeByHourTag(taskDbConfigItem.getTag(),data.getTime()))
									continue;
							}							
							if (taskDbConfigItem.getTableName().equals(TABLE_NAME_DLSJ)){
								if (taskSBDL==null){
									taskSBDL=new TaskDLSJ();
									taskSBDL.setSJID(mp.getDataSaveID());
									taskSBDL.setSJSJ(data.getTime());
									taskSBDL.setBQBJ(bqbj);
									taskSBDL.setCT(mp.getCt());
									taskSBDL.setPT(mp.getPt());			
								}
								PropertyUtils.setProperty(taskSBDL,taskDbConfigItem.getFieldName(),dataItem.getValue());
							}
							else if (taskDbConfigItem.getTableName().equals(TABLE_NAME_FHSJ)){
								if (taskFHSJ==null){
									taskFHSJ=new TaskFHSJ();
									taskFHSJ.setSJID(mp.getDataSaveID());
									taskFHSJ.setSJSJ(data.getTime());
									taskFHSJ.setBQBJ(bqbj);
									taskFHSJ.setCT(mp.getCt());
									taskFHSJ.setPT(mp.getPt());			
								}
								PropertyUtils.setProperty(taskFHSJ,taskDbConfigItem.getFieldName(),dataItem.getValue());
							}
							else if (taskDbConfigItem.getTableName().equals(TABLE_NAME_DLHGLSJ)){
								if (taskDLHGLSJ==null){
									taskDLHGLSJ=new TaskDYHGLSJ();
									taskDLHGLSJ.setSJID(mp.getDataSaveID());
									taskDLHGLSJ.setSJSJ(data.getTime());
									taskDLHGLSJ.setBQBJ(bqbj);
									taskDLHGLSJ.setPT(mp.getPt());			
								}
								PropertyUtils.setProperty(taskDLHGLSJ,taskDbConfigItem.getFieldName(),dataItem.getValue());
							}
						}
					}		
				}
				if (taskSBDL!=null)
					service.addToDao(taskSBDL,Integer.parseInt("10"+deptCode));
				if (taskFHSJ!=null)
					service.addToDao(taskFHSJ,Integer.parseInt("20"+deptCode));
				if (taskDLHGLSJ!=null)
					service.addToDao(taskDLHGLSJ,Integer.parseInt("30"+deptCode));
			}			
		}catch(Exception ex){
			log.error("Error to taskDbConfigAnalyze:"+rtuDatas, ex);
		}
	}
	/*
	 * hourTag,"00":��ʾֻ��������ݲű���;"01"��ʾֻ���������ݲű���
	 */
	public boolean saveJudgeByHourTag(String hourTag,Date date){
		boolean result=false;
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");	
		try{
			String sDate=sdf.format(date);
			if (sDate!=null&&sDate.length()==4){
				if (hourTag.equals("00")){//�����ֲű���
					if (sDate.substring(0,4).equals("0000"))//HH=00���
						result=true;					
				}
				else if(hourTag.equals("01")){
					if (sDate.substring(2,4).equals("00"))//mm=00����
						result=true;
				}
			}		
		}catch(Exception ex){
			log.error("save task date Judge By HourTag err:"+ex);
		}
		return result;
	}
}
