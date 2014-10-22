package com.hzjbbis.fk.bp.processor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.fas.framework.message.MessageZjpb;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.zj.MessageZjHead;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.utils.HexDump;
import com.hzjbbis.fas.model.FaalRequestResponse;
import com.hzjbbis.fas.model.RtuAlert;
import com.hzjbbis.fas.protocol.Protocol;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.handler.ProtocolHandlerFactory;
import com.hzjbbis.fas.protocol.zj.FunctionCode;
import com.hzjbbis.fk.bp.feclient.IntfChannelManage;
import com.hzjbbis.fk.bp.model.AlarmData;
import com.hzjbbis.fk.bp.model.MessageLogErr;
import com.hzjbbis.fk.bp.webapi.MessageWeb;
import com.hzjbbis.fk.bp.webapi.WebMessageEncoder;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.util.ArraysUtil;
import com.hzjbbis.db.batch.event.adapt.BaseExpAlarmHandler;

public class AlarmMessageHandler extends BaseExpAlarmHandler {
	private static final Logger log = Logger.getLogger(AlarmMessageHandler.class);
	/** Web 接口消息编码器 */
    private final WebMessageEncoder encoder = new WebMessageEncoder();
    /** 需要通知自动装接的告警编码,以逗号隔开 */
    private String notifiedAlertCodes;
    private String[] alertCodes;
	public String getNotifiedAlertCodes() {
		return notifiedAlertCodes;
	}
	public void setNotifiedAlertCodes(String notifiedAlertCodes) {
		this.notifiedAlertCodes = notifiedAlertCodes;
		if (notifiedAlertCodes != null) {
			notifiedAlertCodes = notifiedAlertCodes.trim();
            if (notifiedAlertCodes.length() > 0) {
            	this.alertCodes = notifiedAlertCodes.split(",");
            }
        }	
	}
	public void handleExpAlarm(AsyncService service,MessageZj msg){		
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
	        	List<RtuAlert> rtuAlerts=(List<RtuAlert>)value;
	        	//回复异常确认
	        	if(msg.head.msta == 0) {   //主动上送才需要回确认
	        		MessageZj responses = createAlertConfirmMessage(msg, rtuAlerts);
                    if(responses!=null){//异常确认回复
                    	IChannel channel=msg.getSource();
                    	if (channel!=null){
                    		MessageGate gateMsg = new MessageGate();
                    		gateMsg.setDownInnerMessage(responses);
                    		channel.send(gateMsg);
                    		log.info("业务处理器回复异常确认:"+gateMsg.getRawPacketString());
                    	}
                    	else
                    		log.info("up alarm message' source is null:"+msg);
                    }
                }
	        	for(int i=0;i<rtuAlerts.size();i++){
	        		RtuAlert alert = (RtuAlert) rtuAlerts.get(i);
	        		AlarmData alarmData=new AlarmData();	        		
	        		alarmData.setDeptCode(alert.getCorpNo());
	        		alarmData.setRtuId(alert.getRtuId());
	        		alarmData.setCustomerNo(alert.getCustomerNo());
	        		alarmData.setStationNo(alert.getStationNo());
	        		alarmData.setRtuId(alert.getRtuId());
	        		alarmData.setAlertCodeHex(alert.getAlertCodeHex());
	        		alarmData.setAlertTime(alert.getAlertTime());
	        		alarmData.setReceiveTime(alert.getReceiveTime());
	        		alarmData.setSbcs(alert.getSbcs());
	        		alarmData.setTxfs(msg.getTxfs());	        		
	        		String deptCode=alarmData.getDeptCode();					
					if (alert.getDataSaveID()!=null){//测量点未建档，不进行数据库操作
						if (deptCode.length()==4)//单位代码必须是4位,前两位作为分地市保存的key
							deptCode=deptCode.substring(0,2);
						else
							continue;
						alarmData.setDataSaveID(new Long(alert.getDataSaveID()));
						service.addToDao(alarmData,Integer.parseInt("40"+deptCode));
						//告警后处理
						BPLatterProcessor.getInstance().alertDataAdd(alarmData);
					}	        								
					//需要通知自动装接异常告警编码
					if (ArraysUtil.contains(alertCodes,alarmData.getAlertCodeHex())) {
						sendIntfRequestResponse(alarmData);
			        }
					
	        	}	        	
	        }
		}catch (Exception ex) {//非法异常报文保存
			MessageLogErr msgLogErr=new MessageLogErr();
			msgLogErr.setLogicAddress(HexDump.toHex(msg.head.rtua));
			msgLogErr.setQym(msgLogErr.getLogicAddress().substring(0,2));
			msgLogErr.setKzm(Integer.toString(msg.head.c_func, 16));			
			msgLogErr.setTime(new Date(msg.getIoTime()));
			msgLogErr.setBody(msg.getRawPacketString());					
			service.addToDao(msgLogErr,Integer.parseInt("5002"));
            log.error("Error to processing alarm message:"+msg, ex);
        }            
	}
	/**
     * 通知自动装接异常告警编码
     */
	public void sendIntfRequestResponse(AlarmData alarmData){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(new Long(0));
			requestResponse.setRtuId(alarmData.getRtuId());
			Map<String,String> params=new HashMap<String,String>();
			params.put(alarmData.getAlertCodeHex(), alarmData.getAlertCodeHex());
			requestResponse.setParams(params);
			MessageWeb msgWeb=encoder.encode(requestResponse);
	        IntfChannelManage.getInstance().sendMessage(msgWeb);	
		}catch(Exception ex){
			log.error("IntfChannelManage send alarm message err:"+ex);
		}
	}
	/**
     * 创建告警应答消息
     * @param receivedMessage 接收到的消息
     * @param alerts 告警列表
     * @return 应答消息
     */
    private MessageZj createAlertConfirmMessage(MessageZj receivedMessage, List<RtuAlert> alerts) {
        MessageZj msg = createConfirmMessage(receivedMessage);
        if (msg == null) {
            return null;
        }
        
        msg.head.c_func = FunctionCode.CONFIRM_ALERT;
        ByteBuffer data = ByteBuffer.allocate(alerts.size() * 3);
        data.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < alerts.size(); i++) {
            RtuAlert alert = (RtuAlert) alerts.get(i);
            data.put(Byte.parseByte(alert.getTn()));
            data.putShort((short) alert.getAlertCode());
        }
        
        data.flip();
        msg.head.dlen = (short) data.limit();
        msg.data = data;
        return msg;
    }
    /**
     * 创建应答消息
     * @param receivedMessage 接收到的消息
     * @return 应答消息
     */
    private MessageZj createConfirmMessage(MessageZj receivedMessage) {
        // 如果是异常消息，则不需要应答
        if (receivedMessage.head.c_expflag == (byte) 0x01) {
            return null;
        }
        
        long txsj=receivedMessage.getIoTime();     
    	try{		
    		long delt=System.currentTimeMillis()-txsj;
    		if(delt>1800000){	//1800秒后不在回告警应答
    			return null;
    		}
    	}catch(Exception e){
    		
    	}      
        MessageZjHead head = new MessageZjHead();
        MessageZj msg=new MessageZj();
        head.rtua = receivedMessage.head.rtua;
        head.msta = receivedMessage.head.msta;
        head.fseq = receivedMessage.head.fseq;
        head.iseq = 0;
        head.c_dir = 0;
        head.c_expflag = 0;
        head.c_func = receivedMessage.head.c_func;
        head.dlen = 0;
        msg.head = head;
        return msg;
    }
}
