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
 * �㽭��Լ������
 * @author ������
 */
public class PrototalHandlerImpl implements ProtocolHandler {
    
    private static final Log log = LogFactory.getLog(PrototalHandlerImpl.class);
    // �����س�������
    /** ��ǰ�û�ˢ���ն˵���ʱʹ�õĲ������� */
    private static final String RS_PARAM_CODE = "7101";
    /** �����е��ն˲����ı��롣��Щ�����·��ɹ�����Ҫˢ�»��� */
    private static final String[] RTU_PARAM_CODES = {"8011", "8801", "8021", "8022","8010"};
    /** ���ն˲�����Ӧ�������� */
    private static final String[] RTU_PARAM_PROPS = {"smsGateNum", "powerVoltage", "loAuthPassword", "hiAuthPassword","mcommAddress"};
    /** �����еĲ���������ı��롣��Щ�����·��ɹ�����Ҫˢ�»��� */
    private static final String[] MP_PARAM_CODES = {"8902", "8903", "8904", "8911", "8912"};
    /** ������������Ӧ�������� */
    private static final String[] MP_PARAM_PROPS = {"address", "protocol", "port", "ctStr", "ptStr"};
    /** �ն�����ʹ�õĲ��������ǰ׺ */
    private static final String TASK_PARAM_CODE_PREFIX = "81";
    /** �ն��������С����� */
    private static final int MIN_TASK_NUM = 1;
    /** �ն�������������� */
    private static final int MAX_TASK_NUM = 253;
    /** ����ִ�гɹ��ķ����� */
    private static final String CMD_OK = ParseTool.ByteToHex(ErrorCode.CMD_OK);
    
    // �Զ���澯
    /** �Զ���澯���룺�޷����������� */
    private static final int ALERT_CODE_UNPARSEABLE_TASK = 0xF002;
    /** �Զ���澯���룺�޷���ȫ�������ն˸澯 */
    private static final int ALERT_CODE_UNPARSEABLE_ALERT = 0xF003;
    /** �Զ���澯�Ĳ��������� */
    private static final String ALERT_ARG_TASKNUM = "8F04";
    /** �Զ���澯�Ĳ�����澯���� */
    private static final String ALERT_ARG_ALERTCODE = "8F05";
    
    /** ��Ϣ����/���������� */
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
	    // һ�����㽭��Լ֡�����򲻻���뱾��Լ������
    	MessageZj msg = (MessageZj) message;
        int funCode = msg.head.c_func & 0xff;
        
        // ���ڵ�¼����¼�˳�����������Ϣ��������룬ֱ��Ӧ��
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
        
        // ������Ϣ����Ҫ�������
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
     * �����¼�˳���Ϣ
     * @param msg �����Ϣ
     * @return Ӧ����Ϣ���顣�������ҪӦ���򷵻�null
     */
    private IMessage[] processLogoffMessage(MessageZj msg) {
        IMessage[] responses = null;
        
        return responses;
    }
    
    /**
     * ����������Ϣ
     * @param msg �����Ϣ
     * @return Ӧ����Ϣ���顣�������ҪӦ���򷵻�null
     */
    private IMessage[] processHeartbeatMessage(MessageZj msg) {
        IMessage[] responses = null;
       
        return responses;
    }
    
   
    
    /**
     * �ж��Ƿ�Ϊ�ն��ϱ�����Ϣ�������ų���վ�������쳣��Ϣ
     * @param msg �����Ϣ
     * @return true - ���Ϣ��false - ��վ��������Ϣ
     */
    private boolean isRtuMessage(MessageZj msg) {
        if (msg.head.c_expflag == (byte) 0x00) {
            return true;
        }
        
        byte errorCode = msg.data.get(0);
        return errorCode >= 0 && errorCode <= ErrorCode.CMD_TIMEOUT;
    }
    
    /**
     * ����Ӧ����Ϣ
     * @param receivedMessage ���յ�����Ϣ
     * @return Ӧ����Ϣ
     */
    private MessageZj createConfirmMessage(MessageZj receivedMessage) {
        // ������쳣��Ϣ������ҪӦ��
        if (receivedMessage.head.c_expflag == (byte) 0x01) {
            return null;
        }
        
        Long time=receivedMessage.getIoTime();
        if(time!=null){
        	try{        		
        		long delt=System.currentTimeMillis()-time;
        		if(delt>1800000){	//1800����ڻظ澯Ӧ��
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
     * �����쳣Ӧ����Ϣ
     * @param receivedMessage ���յ�����Ϣ
     * @param errorCode �������
     * @return Ӧ����Ϣ
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
     * �����澯Ӧ����Ϣ
     * @param receivedMessage ���յ�����Ϣ
     * @param alerts �澯�б�
     * @return Ӧ����Ϣ
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
     * �����Ϣ����/�����������ģ��������δ�ܽ���������Ż�澯���룬�򴴽���վ�澯
     * @param msg �㽭��Լ��Ϣ
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
