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
	//上报电量数据表名
	private static final String TABLE_NAME_DLSJ="TEMP_SB_DLSJ";
	//上报负荷数据表名
	private static final String TABLE_NAME_FHSJ="TEMP_SB_FHSJ";
	//上报电压合格率数据表名
	private static final String TABLE_NAME_DLHGLSJ="TEMP_SB_DYHGLSJ";
	//任务后处理针对的数据编码
    private String[] taskCodes=new String[]{"8E62","B630"};
	
	public void handleReadTask(AsyncService service,MessageZj msg){
		try {  
			//分辨终端规约:浙江规约或浙江配变规约
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
	    	//调用规约解析报文
	    	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
	        ProtocolHandler handler = factory.getProtocolHandler(messageType);                	
	        Object value = handler.process(msg);  
	        if (value!=null){
	        	int bqbj=0;
	        	if (msg.head.msta!=0)//主站地址用于判断报文是否为主动上送:0终端主动上送;非0召测返回
	        		bqbj=1;
	        	taskDbConfigAnalyze(rtu,service,(List<RtuData>)value,bqbj,msg);
	        }
		}catch (Exception ex) {//任务解析失败需要保存非法任务帧处理			
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
	 * 把任务解析结果RtuData队列根据任务属性分别保存到长表或短表;
	 * 长表保存需要把每个数据项插入对应的数据库表映射对象的属性,
	 * 不同类型任务形成多个或单个数据库表映射对象,分11地市保存01~11
	 * 99表示短表类型key=1000,其他都为长表key={1001~1011,2001~2011,3001~3011}.
	 * */
	public void taskDbConfigAnalyze(BizRtu rtu,AsyncService service,List<RtuData> rtuDatas,int bqbj,MessageZj msg){
		try{
			for (int i = 0; i < rtuDatas.size(); i++) {
				String deptCode=rtu.getDeptCode();
				if (deptCode.length()==4)//单位代码必须是4位,前两位作为分地市保存的key
					deptCode=deptCode.substring(0,2);
				else{
					log.error("终端"+rtu.getLogicAddress()+"的单位代码"+deptCode+"非法");
					saveErrMsgToDb(msg);
					continue;
				}
				RtuData data= (RtuData) rtuDatas.get(i);
				RtuTask rt=rtu.getRtuTask(data.getTaskNum());
				if (rt==null){
					log.error("终端"+rtu.getLogicAddress()+"的任务"+data.getTaskNum()+"不存在");
					saveErrMsgToDb(msg);
					continue;
				}
				MeasuredPoint mp=rtu.getMeasuredPoint(rt.getTn());
				if (mp==null){
					log.error("终端"+rtu.getLogicAddress()+"的测量点号"+rt.getTn()+"不存在");
					saveErrMsgToDb(msg);
					continue;
				}				
				TaskDLSJ taskSBDL=null;
				TaskFHSJ taskFHSJ=null;
				TaskDYHGLSJ taskDLHGLSJ=null;
				for (int j = 0; j < data.getDataList().size(); j++) {
					RtuDataItem dataItem=(RtuDataItem)data.getDataList().get(j);
					//任务后处理
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
					if (rt.getTaskTemplateProperty().equals("99")){//其他任务类型保存短表
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
					else{//保存长表,算法:把每个数据项插入对应的数据库表映射对象的属性
						TaskDbConfig taskDbConfig=RtuManage.getInstance().getTaskDbConfigInCache(dataItem.getCode());
						if (taskDbConfig==null){
							if (log.isInfoEnabled())
								log.info("sjbm="+dataItem.getCode()+"not find in getSjxMap");
							continue;
						}
						for (int k = 0; k < taskDbConfig.getTaskDbConfigItemList().size(); k++) {
							TaskDbConfigItem taskDbConfigItem=(TaskDbConfigItem)taskDbConfig.getTaskDbConfigItemList().get(k);
							//任务属性匹配，任务模板属性不在当前任务数据属性列表则不保存
							if (!taskDbConfigItem.taskPropertyContains(rt.getTaskTemplateProperty())){
								continue;
							}
							//这里需要增加判断TaskDbConfigItem.tag,"00":表示只有零点数据才保存;"01"表示只有整点数据才保存
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
	 * hourTag,"00":表示只有零点数据才保存;"01"表示只有整点数据才保存
	 */
	public boolean saveJudgeByHourTag(String hourTag,Date date){
		boolean result=false;
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");	
		try{
			String sDate=sdf.format(date);
			if (sDate!=null&&sDate.length()==4){
				if (hourTag.equals("00")){//零点零分才保存
					if (sDate.substring(0,4).equals("0000"))//HH=00零点
						result=true;					
				}
				else if(hourTag.equals("01")){
					if (sDate.substring(2,4).equals("00"))//mm=00整点
						result=true;
				}
			}		
		}catch(Exception ex){
			log.error("save task date Judge By HourTag err:"+ex);
		}
		return result;
	}
}
