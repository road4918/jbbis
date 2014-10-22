package com.hzjbbis.fas.protocol.zjpb.handler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.exception.ProtocolHandleException;
import com.hzjbbis.fk.message.zj.MessageZjHead;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.model.*;
import com.hzjbbis.fas.protocol.codec.MessageCodecFactory;
import com.hzjbbis.fas.protocol.codec.MessageDecoder;
import com.hzjbbis.fas.protocol.codec.MessageEncoder;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.zj.ErrorCode;
import com.hzjbbis.fas.protocol.zj.FunctionCode;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * 浙江规约处理器
 * @author 张文亮
 */
public class PrototalHandlerImpl implements ProtocolHandler {
    
    private static final Log log = LogFactory.getLog(PrototalHandlerImpl.class);
    // 浙规相关常量定义
    /** 向前置机刷新终端档案时使用的参数编码 */
    private static final String RS_PARAM_CODE = "7101";
    /** 缓存中的终端参数的编码。这些参数下发成功后需要刷新缓存 */
    private static final String[] RTU_PARAM_CODES = {"8011", "8801", "8021", "8022","8010"};
    /** 与终端参数对应的属性名 */
    private static final String[] RTU_PARAM_PROPS = {"smsGateNum", "powerVoltage", "loAuthPassword", "hiAuthPassword","mcommAddress"};
    /** 缓存中的测量点参数的编码。这些参数下发成功后需要刷新缓存 */
    private static final String[] MP_PARAM_CODES = {"8902", "8903", "8904", "8911", "8912"};
    /** 与测量点参数对应的属性名 */
    private static final String[] MP_PARAM_PROPS = {"address", "protocol", "port", "ctStr", "ptStr"};
    /** 终端任务使用的参数编码的前缀 */
    private static final String TASK_PARAM_CODE_PREFIX = "81";
    /** 终端任务的最小任务号 */
    private static final int MIN_TASK_NUM = 1;
    /** 终端任务的最大任务号 */
    private static final int MAX_TASK_NUM = 253;
    /** 命令执行成功的返回码 */
    private static final String CMD_OK = ParseTool.ByteToHex(ErrorCode.CMD_OK);
    
    // 自定义告警
    /** 自定义告警编码：无法解析的任务 */
    private static final int ALERT_CODE_UNPARSEABLE_TASK = 0xF002;
    /** 自定义告警编码：无法完全解析的终端告警 */
    private static final int ALERT_CODE_UNPARSEABLE_ALERT = 0xF003;
    /** 自定义告警的参数项：任务号 */
    private static final String ALERT_ARG_TASKNUM = "8F04";
    /** 自定义告警的参数项：告警编码 */
    private static final String ALERT_ARG_ALERTCODE = "8F05";
    
    /** 消息编码/解码器工厂 */
	private MessageCodecFactory codecFactory;

	
    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.handler.ProtocolHandler#setCodecFactory(com.hzjbbis.fas.protocol.codec.MessageCodecFactory)
     */
    public void setCodecFactory(MessageCodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.handler.ProtocolHandler#getCodecFactory()
     */
    public MessageCodecFactory getCodecFactory() {        
        return codecFactory;
    }

    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.handler.ProtocolHandler#process(com.hzjbbis.fas.framework.IMessage)
     */
    public IMessage[] process(IMessage message) {
        if (!(message instanceof MessageZj)) {
            throw new ProtocolHandleException("Unsupported message type: " + message.getClass());
        }
    	
        IMessage[] responses = null;
	    // 一定是浙江规约帧，否则不会进入本规约解析器
    	MessageZj msg = (MessageZj) message;
        int funCode = msg.head.c_func & 0xff;
        
        // 对于登录、登录退出、心跳等消息，不需解码，直接应答
        try {
            switch (funCode) {
                case FunctionCode.LOGON:
                    //return processLogonMessage(msg);
                case FunctionCode.LOGOFF:
                	return processLogoffMessage(msg);
                case FunctionCode.HEART_BEAT:
                    return processHeartbeatMessage(msg);
                case FunctionCode.SEND_SMS:                   
            }
        }
        catch (Exception ex) {
            throw new ProtocolHandleException("Error to process message", ex);
        }
        
        // 其它消息，需要解码后处理
    	MessageDecoder decoder = codecFactory.getDecoder(funCode);
        if (decoder == null) {
            throw new ProtocolHandleException("Can't find decoder for function code: " + funCode);
        }
        
        HostCommand cmd = null;
        try {            
            
        	Object value = decoder.decode(msg);
            if (log.isDebugEnabled()) {
                log.debug("Message decoded");
            }
            String txfs=msg.getTxfs();
            
        }
        catch (MessageDecodeException ex) {                   	
            throw ex;
        }
        catch (Exception ex) {            
        	throw new ProtocolHandleException("Error to process message", ex);
        }               
        return responses;
    }

	/* (non-Javadoc)
	 * @see com.hzjbbis.fas.protocol.handler.ProtocolHandler#createMessage(com.hzjbbis.fas.model.FaalRequest)
	 */
	public IMessage[] createMessage(FaalRequest request) {
        MessageEncoder encoder = codecFactory.getEncoder(request.getType());
        if (encoder == null) {
            throw new ProtocolHandleException("Can't find encoder for function code: " + request.getType());
        }
        
        try {
            return encoder.encode(request);
        }
        catch (Exception ex) {
            throw new ProtocolHandleException("Error to encoding message", ex);
        }
	}
    
  
    
    /**
     * 处理登录退出消息
     * @param msg 浙规消息
     * @return 应答消息数组。如果不需要应答，则返回null
     */
    private IMessage[] processLogoffMessage(MessageZj msg) {
        IMessage[] responses = null;
        
        return responses;
    }
    
    /**
     * 处理心跳消息
     * @param msg 浙规消息
     * @return 应答消息数组。如果不需要应答，则返回null
     */
    private IMessage[] processHeartbeatMessage(MessageZj msg) {
        IMessage[] responses = null;
       
        return responses;
    }
    
   
    
    /**
     * 判断是否为终端上报的消息。用于排除主站产生的异常消息
     * @param msg 浙规消息
     * @return true - 活动消息，false - 主站产生的消息
     */
    private boolean isRtuMessage(MessageZj msg) {
        if (msg.head.c_expflag == (byte) 0x00) {
            return true;
        }
        
        byte errorCode = msg.data.get(0);
        return errorCode >= 0 && errorCode <= ErrorCode.CMD_TIMEOUT;
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
        
        Long time=receivedMessage.getIoTime();
        if(time!=null){
        	try{        		
        		long delt=System.currentTimeMillis()-time;
        		if(delt>1800000){	//1800秒后不在回告警应答
        			return null;
        		}
        	}catch(Exception e){
        		//
        	}
        }
        MessageZjHead head = new MessageZjHead();
        head.rtua = receivedMessage.head.rtua;
        head.msta = receivedMessage.head.msta;
        head.fseq = receivedMessage.head.fseq;
        head.iseq = 0;
        head.c_dir = 0;
        head.c_expflag = 0;
        head.c_func = receivedMessage.head.c_func;
        head.dlen = 0;
        receivedMessage.head = head;
        return receivedMessage;
    }
    
    /**
     * 创建异常应答消息
     * @param receivedMessage 接收到的消息
     * @param errorCode 错误编码
     * @return 应答消息
     */
    private MessageZj createErrorConfirmMessage(MessageZj receivedMessage, byte errorCode) {
        MessageZj msg = createConfirmMessage(receivedMessage);
        if (msg == null) {
            return null;
        }
        
        ByteBuffer data = ByteBuffer.wrap(new byte[]{errorCode});
        msg.head.dlen = 1;
        msg.data = data;
        return msg;
    }
    
    /**
     * 创建告警应答消息
     * @param receivedMessage 接收到的消息
     * @param alerts 告警列表
     * @return 应答消息
     */
    private MessageZj createAlertConfirmMessage(MessageZj receivedMessage, List alerts) {
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
     * 检查消息编码/解码器上下文，如果存在未能解析的任务号或告警编码，则创建主站告警
     * @param msg 浙江规约消息
     */
    private void checkMessageCodecContext(MessageZj msg) {
//        String taskNum = MessageCodecContext.pollTaskNum();
//        List alertCodes = MessageCodecContext.pollAlertCodes();
//        if (taskNum == null && alertCodes == null) {
//            return;
//        }
//        
//        Rtu rtu = RtuCache.getInstance().getRtu(msg.getRtuaIn());
//        if (rtu == null) {
//            return;
//        }
//        
//        List alerts = new ArrayList();
//        if (taskNum != null) {
//            RtuAlert alert = createAlert(rtu, ALERT_CODE_UNPARSEABLE_TASK);
//            RtuAlertArg arg = new RtuAlertArg();
//            arg.setCode(ALERT_ARG_TASKNUM);
//            arg.setValue(taskNum);
//            alert.addAlertArg(arg);
//            alerts.add(alert);
//        }
//        
//        if (alertCodes != null) {
//            for (int i = 0; i < alertCodes.size(); i++) {
//                RtuAlert alert = createAlert(rtu, ALERT_CODE_UNPARSEABLE_ALERT);
//                RtuAlertArg arg = new RtuAlertArg();
//                arg.setCode(ALERT_ARG_ALERTCODE);
//                arg.setValue(HexDump.toHex(((Integer) alertCodes.get(i)).shortValue()));
//                alert.addAlertArg(arg);
//                alerts.add(alert);
//            }
//        }
//        
//        if (!alerts.isEmpty()) {
//            FasService service = FasServiceFactory.newFasService();
//            service.createRtuAlerts(alerts, null);
//        }
    }
    
   
}
