/**
 * WEBӦ�ýӿ�Socket���������¼�������
 * ���ܸ�����
 * 		�����յ�WebӦ�����б��ġ����б��ķ��ͳɹ��¼���
 *      Web���б���ͨ��BPMessageQueue���󷽷�ֱ�ӷ��͸�ͨ��ǰ�û���
 * ����ʵ�֣�
 * BasicEventHook�����ࡣ
 * override handleEvent���������ReceiveMessageEvent��SendMessageEvent�ر���
 * ע�������spring�����ļ��У�source���������WEBӦ�ýӿ�Socket��������SocketServer����
 */
package com.hzjbbis.fk.bp.webapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.db.bizprocess.MasterDbService;
import com.hzjbbis.db.managertu.ManageRtu;
import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.exception.MessageEncodeException;
import com.hzjbbis.exception.ProtocolHandleException;
import com.hzjbbis.fas.framework.message.MessageZjpb;
import com.hzjbbis.fas.model.FaalRealTimeWriteParamsRequest;
import com.hzjbbis.fas.model.FaalRefreshCacheRequest;
import com.hzjbbis.fas.model.FaalRequest;
import com.hzjbbis.fas.model.FaalRequestParam;
import com.hzjbbis.fas.model.FaalSendSmsRequest;
import com.hzjbbis.fas.model.FaalWriteParamsRequest;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.protocol.Protocol;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.handler.ProtocolHandlerFactory;
import com.hzjbbis.fk.bp.model.HostCommandDb;
import com.hzjbbis.fk.bp.model.HostCommandItemDb;
import com.hzjbbis.fk.bp.model.HostParamResult;
import com.hzjbbis.fk.bp.model.RtuCommandIdInfo;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IMessageQueue;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.GateHead;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageLoader4Zj;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.sockserver.event.SendMessageEvent;
import com.hzjbbis.fk.utils.CopyUtil;
import com.hzjbbis.fk.utils.HexDump;

public class WebAPIServerEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(WebAPIServerEventHandler.class);
	//����������
	private IMessageQueue msgQueue;				//spring ����ʵ�֡�
	private MasterDbService masterDbService;  	//spring ����ʵ�֡�
	private ManageRtu manageRtu;				//spring ����ʵ�֡�
	
	private MessageLoader4Zj msgLoader=new MessageLoader4Zj();
    /** Web �ӿ���Ϣ������ */
    private final WebMessageDecoder decoder = new WebMessageDecoder();  
    /** Web �ӿ���Ϣ������ */
    //private final WebMessageEncoder encoder = new WebMessageEncoder();
    
	public WebAPIServerEventHandler(){
	}
	
	@Override
	public boolean start() {
		return super.start();
	}
	
	public void setMsgQueue(IMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
	
	/**
	 * ���ظ÷�����
	 */
	public void handleEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			//���յ�ҵ���������б���
			onRecvMessage( (ReceiveMessageEvent)e);
		}
		else if( e.getType() == EventType.MSG_SENT ){
			//���ɹ��ѱ��ķ��͸�ҵ������
			onSendMessage( (SendMessageEvent)e );
		}
		else
			super.handleEvent(e);
	}

	/**
	 * �յ�ҵ�����������б���
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		//�������ع�Լ���ģ���Ҫת�����㽭��Լ���ſ��Է��͸��㽭�նˡ�
		IMessage msgObj = e.getMessage();
		if( msgObj.getMessageType() != MessageType.MSG_WEB ){
			log.error("�յ���Web���ù�Լ�ı���:"+msgObj);
			return;
		}
		MessageWeb msg = (MessageWeb)msgObj;
        if (log.isDebugEnabled()) {
            log.debug("Receive a WebMessage[head: " + (13 + msg.getHead().getHeadAttrLen())
                    + " bytes, body: " + msg.getData().limit() + " bytes]");
        }				
		//����ȷ��
		if( msg.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){			
			return;		//�����������
		}
        try {
            // �� Web ������Ϣ����� Web �������
            Object obj = decoder.decode(msg);
            if (!(obj instanceof FaalRequest)) {
                log.error("Decode WebMessage, the result is not a FaalRequest: " + obj);
                return;
            }
            FaalRequest request = (FaalRequest) obj;
            if (log.isDebugEnabled()) {
                log.debug("Decode WebMessage to FaalRequest: " + request);
            }
            if (request.getOperator()!=null&&request.getOperator().equalsIgnoreCase("zdzj"))
            	log.info("rev msg from zdzj"+request);
            
            // �����ˢ�»���������ˢ���ն˻�������Ӧ�Ķ���
            if (request instanceof FaalRefreshCacheRequest) {
                refreshRtuCache((FaalRefreshCacheRequest) request);
                if (log.isDebugEnabled()) {
                    log.debug("ˢ���ն˻����ѳɹ�");
                }
                return;
            }
                       
            
            //����������Ͷ�������
            if (request instanceof FaalSendSmsRequest) {
            	sendSmsRequest((FaalSendSmsRequest)request);
                return;
            }
            
            // ��������������󱻹��˵�������ת��������
            if (filterRequest(request)) {
                return;
            }
            
            // �������е��ն˰���Լ���ͷ��࣬��ֳɶ������
            List<FaalRequest> requests = classifyRequest(request);
            for (int i = 0; i < requests.size(); i++) {
                sendRequest((FaalRequest) requests.get(i));
            }
        }
        catch (MessageDecodeException ex) {
            log.error("Error to decode WebMessage", ex);
        }
        catch (MessageEncodeException ex) {
            log.error("Error to encode FaalRequest", ex);
        }
        catch (Exception ex) {
            log.error("Error to send FaalRequest", ex);
        }
	}
	
	/**
	 * ��ҵ���������б��ĳɹ���
	 * @param e
	 */
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		if( log.isDebugEnabled() )
			log.debug("��WEB���ͱ��ĳɹ�:"+msg);
	}
	

    /**
     * �� FAAL ͨѶ���󰴹�Լ������
     * @param request FAAL ͨѶ����
     * @return ����Լ�������� FAAL ͨѶ�����б�
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private List<FaalRequest> classifyRequest(FaalRequest request) throws InstantiationException, IllegalAccessException {
        List<FaalRequest> requests = new ArrayList<FaalRequest>();
        // ��������Ѿ���ȷָ���˹�Լ���ͣ�����Ҫ����
        if (request.getProtocol() != null) {
            requests.add(request);
            return requests;
        }
        
        List<String> rtuIds = request.getRtuIds();
        // ���������û��ָ���նˣ����޷����࣬Ĭ��Ϊ�㽭��Լ
        if (rtuIds == null || rtuIds.isEmpty()) {
            request.setProtocol(Protocol.ZJ);
            requests.add(request);
            return requests;
        }
        
        // ���ն�ID�б������ID�б���Լ���ͷֿ�
        HashMap<String,List<String>> rtuIdMap = new HashMap<String,List<String>>();
        HashMap<String,List<Long>> cmdIdMap = new HashMap<String,List<Long>>();
        for (int i = 0; i < rtuIds.size(); i++) {
            BizRtu rtu = RtuManage.getInstance().getBizRtuInCache((String) rtuIds.get(i));
            if(rtu==null){
            	//�Ҳ����ն���ˢ�����ݿ⵵��
				boolean refreshTag=manageRtu.refreshBizRtu((String) rtuIds.get(i));
				if (!refreshTag){
					log.error("not find rtuId in db:"+(String) rtuIds.get(i));
					continue;
				}
				else
					rtu=(RtuManage.getInstance().getBizRtuInCache((String) rtuIds.get(i)));    			            	
            }
            String protocol = rtu.getRtuProtocol();
            List<String> l = (List<String>) rtuIdMap.get(protocol);
            if (l == null) {
                l = new ArrayList<String>();
                rtuIdMap.put(protocol, l);
                cmdIdMap.put(protocol, new ArrayList<Long>());
            }            
            l.add((String)rtuIds.get(i));
            List<Long> l2 = (List<Long>) cmdIdMap.get(protocol);
            l2.add((Long)request.getCmdIds().get(i));
        }       
        // ���ֻ��һ�ֹ�Լ����ֱ����������Ĺ�Լ����
        if (rtuIdMap.size() == 1) {
            request.setProtocol((String) rtuIdMap.keySet().iterator().next());
            requests.add(request);
            return requests;
        }
      
        // Ϊÿ�ֹ�Լ����һ���µ�����
        Iterator<String> it = rtuIdMap.keySet().iterator();
        while (it.hasNext()) {
            String protocol = (String) it.next();
            FaalRequest newReq = (FaalRequest) request.getClass().newInstance();
            CopyUtil.copyProperties(newReq, request);
            newReq.setProtocol(protocol);
            newReq.setRtuIds((List<String>) rtuIdMap.get(protocol));
            newReq.setCmdIds((List<Long>) cmdIdMap.get(protocol));
            requests.add(newReq);
        }
        
        return requests;
    }
    
    /**
     * ���Ͷ�������  
     * �ն˾ֺ��б�(rtuIds)Ϊ�ձ�ʾ���û�������Ϣ,����Ϊ�ն��Զ������
     * ����ID�б�(smsids)��Ϊ�ձ�ʾָ������ͨ������,����Ĭ��ͨ������
     * @param smsrequest
     */
    private void sendSmsRequest(FaalSendSmsRequest smsrequest){
    	try{		    	
	        List<String> rtuIds=smsrequest.getRtuIds(); 
	        List<String> smsids=smsrequest.getSmsids();
	        String smsId="";
	        if (smsids!=null){//����Ӧ�ú�,����:95598305001
	        	String str=(String)smsids.get(0);
	        	if (str.length()>=9&&str.length()<=11){
	        		if (str.substring(0,5).equals("95598"))
	        			smsId=str;
	        	}
	        	if (smsId.equals(""))
	        		log.error("������վ��������,ָ������ͨ������:"+str);
	        }
	        if (rtuIds==null){//�ն˾ֺ��б�Ϊ�ձ�ʾ���û�������Ϣ
	        	//������������Ϊ�ض���Լ����Ϣ
	    		ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
		        ProtocolHandler handler = factory.getProtocolHandler(MessageZj.class);
	        	IMessage[] messages = handler.createMessage(smsrequest);
		        //ת����Ϣ
		        for (int i = 0; i < messages.length; i++) {
		        	MessageZj msgZj = (MessageZj)messages[i]; 		        		        		
		        	MessageGate gateMsg = new MessageGate();
		        	if (!smsId.equals("")){
		        		gateMsg.getHead().setAttribute(GateHead.ATT_DESTADDR,smsId);
		        	}	
	        		gateMsg.setDownInnerMessage(msgZj);
	                msgQueue.sendMessage(gateMsg);
		        }
	        }else{//���ն˷����Զ������
	        	MessageZj msgZj=msgLoader.loadMessage(smsrequest.getContent());	        	
	        	MessageGate gateMsg = new MessageGate();
        		gateMsg.setDownInnerMessage(msgZj);
        		if (!smsId.equals(""))
        			gateMsg.getHead().setAttribute(GateHead.ATT_DESTADDR,smsId);
                msgQueue.sendMessage(gateMsg);
	        }	        	        	      	        
    	}catch(Exception e){
    		log.error("������վ��������",e);
    	}
    }
    
    /**
     * ���� FAAL ͨѶ����
     * @param request FAAL ͨѶ����
     */
    private void sendRequest(FaalRequest request) {
        // ȷ����Լ��Ϣ���ͣ��ж����㽭��Լ���ǹ���96��Լ��ȷ����Լ��Ϣ����
        Class messageType = null;
        if (Protocol.ZJ.equals(request.getProtocol())) {
            messageType = MessageZj.class;
        }
		else if(Protocol.ZJPB.equals(request.getProtocol())){
			messageType = MessageZjpb.class;
		}
        else {
            throw new ProtocolHandleException("Unsported protocol: " + request.getProtocol());
        }
        
        // ������������Ϊ�ض���Լ����Ϣ
        try{
        	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
            ProtocolHandler handler = factory.getProtocolHandler(messageType);
            IMessage[] messages = handler.createMessage(request);
            if (log.isDebugEnabled()) {
                log.debug("Encode to Message, protocol: " + request.getProtocol()
                        + ", message count: " + messages.length);
            }            
            // ת����Ϣ
            for (int i = 0; i < messages.length; i++) {
                if(messages[i].getStatus()!=null){
            		if(HostCommand.STATUS_PARA_INVALID.equalsIgnoreCase(messages[i].getStatus())){
            			//��֡ʧ��,��д״̬
            			errorHostCommand(messages[i].getCmdId());
            			continue;
            		}                	
                } 
                MessageZj msgZj = (MessageZj)messages[i];                
                int ifseq=masterDbService.getRtuCommandSeq(HexDump.toHex(msgZj.head.rtua));
                msgZj.head.fseq=(byte)ifseq;
        		MessageGate gateMsg = new MessageGate();
        		gateMsg.setDownInnerMessage(msgZj);
                msgQueue.sendMessage(gateMsg);
                //�����ն��߼���ַ,֡���,����ID�����ݿ��,�������з��ض�Ӧ
                RtuCommandIdInfo rtuCmd=new RtuCommandIdInfo();
                if (request.getOperator()!=null
                	&&request.getOperator().equalsIgnoreCase("zdzj"))	//�Զ�װ������
                	rtuCmd.setZdzjbz(1);
                else								 				//��վ����
                	rtuCmd.setZdzjbz(0);
        		rtuCmd.setLogicAddress(HexDump.toHex(msgZj.head.rtua));
        		rtuCmd.setZxh(ifseq);
        		rtuCmd.setCmdId(msgZj.getCmdId());
        		rtuCmd.setBwsl(msgZj.getMsgCount());
        		masterDbService.insertRtuComdMag(rtuCmd);        		
            }
            if (log.isDebugEnabled()) {
                log.debug(messages.length + " messages send");
            }
        }catch(MessageEncodeException e){
        	//��֡���󣬷�����վ������
        	try{
        		if (e.getCode()!=null){//������������ݷǷ���ط������ô���ı�ʶ
        			List<Long> cmds=request.getCmdIds();
            		for(Iterator<Long> iter=cmds.iterator();iter.hasNext();){
            			Long cmdId=(Long)iter.next();
            			if (cmdId==0)//��̨������������
            				errorBackgroundCommand(request,e.getCode());
            			else//��վ����
            				errorHostCommand(cmdId);
            		}	
        		}        		
        	}catch(Exception ex){
        		log.error("update error host cmd status",ex);
        	}        	
        }  
        catch(ProtocolHandleException e){  
        	log.error("request to msg",e);
        }
    }
    
    
    
    /**
     * ˢ���ն˻���
     * �ն˾ֺ��б�Ϊ�ձ�ʾˢ���ն���ص�����Ϣ(�����ʲ�,������,�ն�����)
     * ��������Ϊ0,�������������������ӳ����Ϣ;��0��ʾ���ʾˢ��ָ��ģ��ID������ģ��
     * @param request ˢ�»����������
     */
    private void refreshRtuCache(FaalRefreshCacheRequest request) {
        List<String> rtuIds = request.getRtuIds();
        if (rtuIds != null && !rtuIds.isEmpty()) {
        	for (int i = 0; i < rtuIds.size(); i++) {
                String rtuId = (String) rtuIds.get(i);
                boolean refresh=manageRtu.refreshBizRtu(rtuId);     
                log.info("refresh rtuId tag="+refresh);
            }
        }       
        String taskNum = request.getTaskNum();
        if (taskNum!=null){
        	if (taskNum.equals("0"))//�������������������ӳ����Ϣ
        		manageRtu.initializeTaskDbConfig();        		        	
        	else if(taskNum.equals("-1"))//��������ϵͳ���ò���
        		manageRtu.initializeSysConfig();        		
        	
        	else					//ˢ��ָ��ģ��ID������ģ��
        		manageRtu.refreshTaskTemplate(taskNum);
        	log.info("refresh taskNum ="+taskNum);
        }
    }
    
    
    /**
     * ���� Faal ͨѶ����
     * @param request Faal ͨѶ����
     * @return true - �����ѱ����ˣ�����Ҫת����false - ������Ҫת��
     */
    private boolean filterRequest(FaalRequest request) {
        if (request instanceof FaalWriteParamsRequest
                || request instanceof FaalRealTimeWriteParamsRequest) {
            List<FaalRequestParam> params = request.getParams();
            if(params==null){	
            	//��֡���󣬷�����վ������
            	try{
            		List<Long> cmds=request.getCmdIds();
            		for(Iterator<Long> iter=cmds.iterator();iter.hasNext();){
            			Long cmdId=(Long)iter.next();
            			//��̨�·����û�д���
            			if (cmdId!=0)
            				errorHostCommand(cmdId);
            		}
            	}catch(Exception ex){            		
            		log.error("updata error host cmd status",ex);
            	}
            	return true;
            }            
            return request.getRtuIds().isEmpty();
        }       
        return false;
    }     
    
    /**
     * ����������޷���֡������
     * @param commandId
     */
    private void errorHostCommand(Long commandId) {   
    	HostCommandDb command = new HostCommandDb();
        command.setId(commandId);
        command.setStatus(HostCommand.STATUS_PRAR_ERROR);
        command.setErrcode(command.getStatus());
        command.setMessageCount(1);
        masterDbService.procUpdateCommandStatus(command);
    }      
    /*
	 * ���º�̨����ʧ�ܽ��
	 * */
	public void errorBackgroundCommand(FaalRequest request,String code) {
		// �����д�������������û�з���ͨѶʧ�ܣ�����²������ý��
		try{
			String tn=null;
			if (request instanceof FaalWriteParamsRequest){
				FaalWriteParamsRequest object=(FaalWriteParamsRequest)request;
				tn=object.getTn();
			}
			else return;
			if (tn!=null){				
				List<String> rtuIds=request.getRtuIds();			
				if (rtuIds != null && !rtuIds.isEmpty()) {        			       						        		
	        		for(int j = 0; j < rtuIds.size(); j++){		        			
	        			HostParamResult paramResult =new HostParamResult();
		        		paramResult.setRtuId((String)rtuIds.get(j));
		            	paramResult.setCode(code);
		            	paramResult.setTn(tn);
	            		paramResult.setStatus((HostCommandItemDb.STATUS_FAILED));
	            		paramResult.setSbyy(HostCommand.STATUS_PRAR_ERROR);
	            		masterDbService.procUpdateParamResult(paramResult); 	 
	        		}       		         	                   	                      
		        }
			}									
		}catch(Exception e){
			log.error("error background Command update error:",e);
		}
	}
	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}

	public void setManageRtu(ManageRtu manageRtu) {
		this.manageRtu = manageRtu;
	}
}
