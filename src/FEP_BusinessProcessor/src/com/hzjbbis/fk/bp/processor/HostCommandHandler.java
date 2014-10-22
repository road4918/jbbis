package com.hzjbbis.fk.bp.processor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.db.bizprocess.MasterDbService;
import com.hzjbbis.db.managertu.ManageRtu;
import com.hzjbbis.fas.framework.message.MessageZjpb;
import com.hzjbbis.fas.model.FaalRequestResponse;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.model.HostCommandResult;
import com.hzjbbis.fas.protocol.Protocol;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.handler.ProtocolHandlerFactory;
import com.hzjbbis.fas.protocol.zj.ErrorCode;
import com.hzjbbis.fas.protocol.zj.FunctionCode;
import com.hzjbbis.fk.bp.feclient.IntfChannelManage;
import com.hzjbbis.fk.bp.model.HostCommandDb;
import com.hzjbbis.fk.bp.model.HostCommandItemDb;
import com.hzjbbis.fk.bp.model.HostParamResult;
import com.hzjbbis.fk.bp.webapi.MessageWeb;
import com.hzjbbis.fk.bp.webapi.WebMessageEncoder;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuCmdItem;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.utils.HexDump;

public class HostCommandHandler {
	private static final Logger log = Logger.getLogger(HostCommandHandler.class);
	/** Web �ӿ���Ϣ������ */
    private final WebMessageEncoder encoder = new WebMessageEncoder();
	public void handleExpNormalMsg(ManageRtu manageRtu,MasterDbService masterDbService,MessageZj msg){				
		BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(msg.head.rtua));
		if (rtu==null){
			log.error("not find rtu in cache:"+HexDump.toHex(msg.head.rtua));
		}
		try{   		
			//�ֱ��ն˹�Լ:�㽭��Լ���㽭����Լ
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
	        List<RtuCmdItem> rcis=masterDbService.getRtuComdItem(HexDump.toHex(msg.head.rtua), msg.head.fseq);
			if (rcis.size()>1)//��Ӧ���ж��,ֻ������Ψһ��
				log.error("getGetRtuComdItem size>1:"+HexDump.toHex(msg.head.rtua)+";"+msg.head.fseq);
	        if (value!=null){
	        	HostCommand cmd = (HostCommand) value;
	        	//��Ҫͨ���ն��߼���ַ+������Ų�ѯ����ID,�������ID��ѯ���ΪNULL��Ϊ0,��Ϊ��̨�·�����,����Ҫ���������б� 	
	        	boolean setTag=false;
	        	//�ж��Ƿ�Ϊ���÷���
	        	if (msg.head.c_func==FunctionCode.WRITE_PARAMS||msg.head.c_func==FunctionCode.REALTIME_WRITE_PARAMS)
	        		setTag=true;
	        	for(RtuCmdItem rci:rcis){
	        		long cmdId=rci.getCmdId();
	        		if (cmdId!= 0) {//��վʵʱ����	        		
		        		cmd.setId(new Long(cmdId));	
		        		cmd.setMessageCount(rci.getBwsl());
		        		if (rci.getZdzjbz()==1){//�Զ�װ�����󷵻�
		        			sendIntfRequestResponse(cmd,rtu.getRtuId(),setTag);
		        		}
		        		else//��վ����
		        			updateHostCommand(masterDbService,cmd);
		        	}	 
	        	}	        	
        		if (setTag){//���÷���        			        			
        			updateParaTable(manageRtu,masterDbService,cmd,rtu.getRtuId());
        		}
	        }
	        else{//����ʧ��,��д״̬
	        	for(RtuCmdItem rci:rcis){
	        		long cmdId=rci.getCmdId();
	        		if (cmdId!= 0){ //��վʵʱ����	
	        			if (rci.getZdzjbz()==1){//�Զ�װ�����󷵻�
		        			sendIntfErrorResponse(cmdId,rtu.getRtuId());
		        		}
	        			else
	        				errorProcessMessage(masterDbService,cmdId);
	        		}
	        	}
	        }
		}catch(Exception ex){//��������,��д״̬
			List<RtuCmdItem> rcis=masterDbService.getRtuComdItem(HexDump.toHex(msg.head.rtua), msg.head.fseq);
			if (rcis.size()>1)//��Ӧ���ж��,ֻ������Ψһ��
				log.error("getGetRtuComdItem size>1:"+HexDump.toHex(msg.head.rtua)+";"+msg.head.fseq);
			for(RtuCmdItem rci:rcis){
        		long cmdId=rci.getCmdId();
        		if (cmdId!= 0){ //��վʵʱ����	 
        			if (rci.getZdzjbz()==1){//�Զ�װ�����󷵻�
	        			sendIntfErrorResponse(cmdId,rtu.getRtuId());
	        		}
        			else
        				errorProcessMessage(masterDbService,cmdId);
        		}
        	}
			log.error("Error to processing Normal message:"+msg, ex);
		}
	}
	/**
	 * �Զ�װ�����ݷ��ط���
	 * @param command
	 * @param rtuId
	 * @param setTag
	 */
	public void sendIntfRequestResponse(HostCommand command,String rtuId,boolean setTag){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(command.getId());
			requestResponse.setRtuId(rtuId);
			requestResponse.setCmdStatus(command.getStatus());
			Map<String,String> params=new HashMap<String,String>();
			List<HostCommandResult> results = command.getResults(); 
			if (results != null && !results.isEmpty()) {
	            for (int i = 0; i < results.size(); i++) {
	            	HostCommandResult result=(HostCommandResult) results.get(i); 
	            	if (result.getValue()!=null){
	            		if (setTag)//���÷���
	            			params.put(result.getCode(),ErrorCode.toHostCommandStatus(Byte.parseByte(result.getValue())));
	            		else//��ȡ����
	            			params.put(result.getCode(),result.getValue());
	            	}
	            		           	          
	            }
	            requestResponse.setParams(params);	            
	        }  
			MessageWeb msgWeb=encoder.encode(requestResponse);
            IntfChannelManage.getInstance().sendMessage(msgWeb);
		}catch(Exception ex){
			log.error("IntfChannelManage send host message err:"+ex);
		}
	}
	public void sendIntfErrorResponse(Long cmdId,String rtuId){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(cmdId);
			requestResponse.setRtuId(rtuId);
			requestResponse.setCmdStatus(HostCommand.STATUS_PARSE_ERROR);
			MessageWeb msgWeb=encoder.encode(requestResponse);
            IntfChannelManage.getInstance().sendMessage(msgWeb);
		}catch(Exception ex){
			log.error("IntfChannelManage send host message err:"+ex);
		}
	}
	/*
	 * ������վ�����������վ���������б�
	 * */
	public void updateHostCommand(MasterDbService masterDbService,HostCommand command) {
        List<HostCommandResult> results = command.getResults();   
        HostCommandDb commandDb=new HostCommandDb();
        commandDb.setId(command.getId());
        commandDb.setMessageCount(command.getMessageCount());
        commandDb.setStatus(command.getStatus());
        commandDb.setErrcode(command.getStatus());
        if (results != null && !results.isEmpty()) {
            // ����ÿ����¼
            for (int i = 0; i < results.size(); i++) {
            	HostCommandResult result=(HostCommandResult) results.get(i); 
            	HostCommandItemDb item=new HostCommandItemDb(); 
            	item.setCommandId(command.getId());
            	item.setTn(result.getTn());
            	item.setAlertCode(result.getAlertCode());
            	item.setCode(result.getCode());
            	item.setValue(result.getValue());
            	item.setTime(new Date());
            	item.setProgramTime(result.getProgramTime());
            	item.setChannel(result.getChannel());
            	masterDbService.insertCommandResult(item);             	          
            }
        }                
        // ��������״̬
        masterDbService.procUpdateCommandStatus(commandDb);
    }
	/*
	 * ������վ��������
	 * */
	public void updateParaTable(ManageRtu manageRtu,MasterDbService masterDbService,HostCommand command,String rtuId) {
		// �����д�������������û�з���ͨѶʧ�ܣ�����²������ý��
		try{
			List<HostCommandResult> results = command.getResults();
        	        
			if (results != null && !results.isEmpty()) {        			       				
	        	for (int i = 0; i < results.size(); i++) {
	        		HostCommandResult item=(HostCommandResult) results.get(i); 
	        		HostParamResult paramResult =new HostParamResult();
	        		paramResult.setRtuId(rtuId);
	            	paramResult.setCode(item.getCode());
	            	paramResult.setTn(item.getTn());
	            	if (ErrorCode.CMD_OK == Byte.parseByte(item.getValue())) {//���óɹ�
	            		paramResult.setStatus((HostCommandItemDb.STATUS_SUCCESS));
	            		paramResult.setSbyy("");
	            		masterDbService.procUpdateParamResult(paramResult);  
	            		if (paramResult.getCode().equals("81FE"))//����״̬����,�����ݿ�ˢ���ڴ��ն�����������Ϣ
	            			manageRtu.refreshRtuTasks(rtuId);
	            	}
	            	else{
	            		paramResult.setStatus((HostCommandItemDb.STATUS_FAILED));
	            		paramResult.setSbyy(ErrorCode.toHostCommandStatus(Byte.parseByte(item.getValue())));
	            		masterDbService.procUpdateParamResult(paramResult);  
	            	}	 
	            	                  	                
	            }
	        }
		}catch(Exception e){
			log.error("update para table",e);
		}
	}
    /**
     * �������󷵻ر���ʧ��
     * @param commandId
     */
    private void errorProcessMessage(MasterDbService masterDbService,Long commandId) {   
    	HostCommandDb command = new HostCommandDb();
        command.setId(commandId);
        command.setStatus(HostCommand.STATUS_PARSE_ERROR);
        command.setErrcode(command.getStatus());
        command.setMessageCount(1);
        masterDbService.procUpdateCommandStatus(command);
    }  
}
