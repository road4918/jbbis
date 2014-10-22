/**
 * WEB应用接口Socket服务器的事件处理器
 * 功能概述：
 * 		侦听收到Web应用下行报文、上行报文发送成功事件。
 *      Web下行报文通过BPMessageQueue对象方法直接发送给通信前置机；
 * 技术实现：
 * BasicEventHook派生类。
 * override handleEvent方法，针对ReceiveMessageEvent和SendMessageEvent特别处理。
 * 注意事项：在spring配置文件中，source对象必须是WEB应用接口Socket服务器的SocketServer对象。
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
	//可配置属性
	private IMessageQueue msgQueue;				//spring 配置实现。
	private MasterDbService masterDbService;  	//spring 配置实现。
	private ManageRtu manageRtu;				//spring 配置实现。
	
	private MessageLoader4Zj msgLoader=new MessageLoader4Zj();
    /** Web 接口消息解码器 */
    private final WebMessageDecoder decoder = new WebMessageDecoder();  
    /** Web 接口消息编码器 */
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
	 * 重载该方法。
	 */
	public void handleEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			//当收到业务处理器下行报文
			onRecvMessage( (ReceiveMessageEvent)e);
		}
		else if( e.getType() == EventType.MSG_SENT ){
			//当成功把报文发送给业务处理器
			onSendMessage( (SendMessageEvent)e );
		}
		else
			super.handleEvent(e);
	}

	/**
	 * 收到业务处理器的下行报文
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		//对于网关规约报文，需要转换成浙江规约，才可以发送给浙江终端。
		IMessage msgObj = e.getMessage();
		if( msgObj.getMessageType() != MessageType.MSG_WEB ){
			log.error("收到非Web调用规约的报文:"+msgObj);
			return;
		}
		MessageWeb msg = (MessageWeb)msgObj;
        if (log.isDebugEnabled()) {
            log.debug("Receive a WebMessage[head: " + (13 + msg.getHead().getHeadAttrLen())
                    + " bytes, body: " + msg.getData().limit() + " bytes]");
        }				
		//心跳确认
		if( msg.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){			
			return;		//心跳处理结束
		}
        try {
            // 将 Web 请求消息解码成 Web 请求对象
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
            
            // 如果是刷新缓存请求，则刷新终端缓存中相应的对象
            if (request instanceof FaalRefreshCacheRequest) {
                refreshRtuCache((FaalRefreshCacheRequest) request);
                if (log.isDebugEnabled()) {
                    log.debug("刷新终端缓存已成功");
                }
                return;
            }
                       
            
            //如果是请求发送短信请求
            if (request instanceof FaalSendSmsRequest) {
            	sendSmsRequest((FaalSendSmsRequest)request);
                return;
            }
            
            // 过滤请求。如果请求被过滤掉，则不再转发，返回
            if (filterRequest(request)) {
                return;
            }
            
            // 将请求中的终端按规约类型分类，拆分成多个请求
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
	 * 往业务处理器上行报文成功。
	 * @param e
	 */
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		if( log.isDebugEnabled() )
			log.debug("往WEB发送报文成功:"+msg);
	}
	

    /**
     * 将 FAAL 通讯请求按规约类别分类
     * @param request FAAL 通讯请求
     * @return 按规约类别分类后的 FAAL 通讯请求列表
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private List<FaalRequest> classifyRequest(FaalRequest request) throws InstantiationException, IllegalAccessException {
        List<FaalRequest> requests = new ArrayList<FaalRequest>();
        // 如果请求已经明确指定了规约类型，则不需要分类
        if (request.getProtocol() != null) {
            requests.add(request);
            return requests;
        }
        
        List<String> rtuIds = request.getRtuIds();
        // 如果请求中没有指定终端，则无法分类，默认为浙江规约
        if (rtuIds == null || rtuIds.isEmpty()) {
            request.setProtocol(Protocol.ZJ);
            requests.add(request);
            return requests;
        }
        
        // 把终端ID列表和命令ID列表按规约类型分开
        HashMap<String,List<String>> rtuIdMap = new HashMap<String,List<String>>();
        HashMap<String,List<Long>> cmdIdMap = new HashMap<String,List<Long>>();
        for (int i = 0; i < rtuIds.size(); i++) {
            BizRtu rtu = RtuManage.getInstance().getBizRtuInCache((String) rtuIds.get(i));
            if(rtu==null){
            	//找不到终端则刷新数据库档案
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
        // 如果只有一种规约，则直接设置请求的规约类型
        if (rtuIdMap.size() == 1) {
            request.setProtocol((String) rtuIdMap.keySet().iterator().next());
            requests.add(request);
            return requests;
        }
      
        // 为每种规约生成一个新的请求
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
     * 发送短信请求  
     * 终端局号列表(rtuIds)为空表示给用户发短消息,否则为终端自定义短信
     * 短信ID列表(smsids)不为空表示指定短信通道发送,否则按默认通道发送
     * @param smsrequest
     */
    private void sendSmsRequest(FaalSendSmsRequest smsrequest){
    	try{		    	
	        List<String> rtuIds=smsrequest.getRtuIds(); 
	        List<String> smsids=smsrequest.getSmsids();
	        String smsId="";
	        if (smsids!=null){//短信应用号,例如:95598305001
	        	String str=(String)smsids.get(0);
	        	if (str.length()>=9&&str.length()<=11){
	        		if (str.substring(0,5).equals("95598"))
	        			smsId=str;
	        	}
	        	if (smsId.equals(""))
	        		log.error("发送主站短信请求,指定短信通道错误:"+str);
	        }
	        if (rtuIds==null){//终端局号列表为空表示给用户发短消息
	        	//将请求对象编码为特定规约的消息
	    		ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
		        ProtocolHandler handler = factory.getProtocolHandler(MessageZj.class);
	        	IMessage[] messages = handler.createMessage(smsrequest);
		        //转发消息
		        for (int i = 0; i < messages.length; i++) {
		        	MessageZj msgZj = (MessageZj)messages[i]; 		        		        		
		        	MessageGate gateMsg = new MessageGate();
		        	if (!smsId.equals("")){
		        		gateMsg.getHead().setAttribute(GateHead.ATT_DESTADDR,smsId);
		        	}	
	        		gateMsg.setDownInnerMessage(msgZj);
	                msgQueue.sendMessage(gateMsg);
		        }
	        }else{//给终端发送自定义短信
	        	MessageZj msgZj=msgLoader.loadMessage(smsrequest.getContent());	        	
	        	MessageGate gateMsg = new MessageGate();
        		gateMsg.setDownInnerMessage(msgZj);
        		if (!smsId.equals(""))
        			gateMsg.getHead().setAttribute(GateHead.ATT_DESTADDR,smsId);
                msgQueue.sendMessage(gateMsg);
	        }	        	        	      	        
    	}catch(Exception e){
    		log.error("发送主站短信请求",e);
    	}
    }
    
    /**
     * 发送 FAAL 通讯请求
     * @param request FAAL 通讯请求
     */
    private void sendRequest(FaalRequest request) {
        // 确定规约消息类型：判断是浙江规约还是国网96规约，确定规约消息类型
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
        
        // 将请求对象编码为特定规约的消息
        try{
        	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
            ProtocolHandler handler = factory.getProtocolHandler(messageType);
            IMessage[] messages = handler.createMessage(request);
            if (log.isDebugEnabled()) {
                log.debug("Encode to Message, protocol: " + request.getProtocol()
                        + ", message count: " + messages.length);
            }            
            // 转发消息
            for (int i = 0; i < messages.length; i++) {
                if(messages[i].getStatus()!=null){
            		if(HostCommand.STATUS_PARA_INVALID.equalsIgnoreCase(messages[i].getStatus())){
            			//组帧失败,回写状态
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
                //插入终端逻辑地址,帧序号,命令ID进数据库表,用于上行返回对应
                RtuCommandIdInfo rtuCmd=new RtuCommandIdInfo();
                if (request.getOperator()!=null
                	&&request.getOperator().equalsIgnoreCase("zdzj"))	//自动装接请求
                	rtuCmd.setZdzjbz(1);
                else								 				//主站请求
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
        	//组帧错误，返回主站错误码
        	try{
        		if (e.getCode()!=null){//如果是设置内容非法则回返回设置错误的标识
        			List<Long> cmds=request.getCmdIds();
            		for(Iterator<Long> iter=cmds.iterator();iter.hasNext();){
            			Long cmdId=(Long)iter.next();
            			if (cmdId==0)//后台服务设置请求
            				errorBackgroundCommand(request,e.getCode());
            			else//主站请求
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
     * 刷新终端缓存
     * 终端局号列表不为空表示刷新终端相关档案信息(包括资产,测量点,终端任务)
     * 如果任务号为0,重新生成任务数据项表映射信息;非0表示则表示刷新指定模版ID的任务模版
     * @param request 刷新缓存请求对象
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
        	if (taskNum.equals("0"))//重新生成任务数据项表映射信息
        		manageRtu.initializeTaskDbConfig();        		        	
        	else if(taskNum.equals("-1"))//重新生成系统配置参数
        		manageRtu.initializeSysConfig();        		
        	
        	else					//刷新指定模版ID的任务模版
        		manageRtu.refreshTaskTemplate(taskNum);
        	log.info("refresh taskNum ="+taskNum);
        }
    }
    
    
    /**
     * 过滤 Faal 通讯请求
     * @param request Faal 通讯请求
     * @return true - 请求已被过滤，不需要转发；false - 请求需要转发
     */
    private boolean filterRequest(FaalRequest request) {
        if (request instanceof FaalWriteParamsRequest
                || request instanceof FaalRealTimeWriteParamsRequest) {
            List<FaalRequestParam> params = request.getParams();
            if(params==null){	
            	//组帧错误，返回主站错误码
            	try{
            		List<Long> cmds=request.getCmdIds();
            		for(Iterator<Long> iter=cmds.iterator();iter.hasNext();){
            			Long cmdId=(Long)iter.next();
            			//后台下发不用回写结果
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
     * 因参数错误，无法组帧的命令
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
	 * 更新后台设置失败结果
	 * */
	public void errorBackgroundCommand(FaalRequest request,String code) {
		// 如果是写参数命令，且命令没有发生通讯失败，则更新参数设置结果
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
