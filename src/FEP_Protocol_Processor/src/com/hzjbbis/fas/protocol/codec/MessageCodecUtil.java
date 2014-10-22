package com.hzjbbis.fas.protocol.codec;

import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.handler.ProtocolHandlerFactory;
import com.hzjbbis.fas.protocol.zj.FunctionCode;

/**
 * ��Ϣ����/���빤�ߡ��ṩ��������ı���/���뷽��
 * @author ������
 */
public abstract class MessageCodecUtil {

    /** Э�鴦�������� */
    private static final ProtocolHandlerFactory handlerFactory = ProtocolHandlerFactory.getInstance();    

    /**
     * ����Ϣ����ɸ��߼���ҵ�����
     * @param rawData ��Ϣ��ԭʼ����
     * @param direction ��Ϣ���䷽��
     * @param messageType ��Ϣ����
     * @return ҵ�����
     */
    /*public static Object decode(String rawData, Integer direction, Class messageType) {
        IMessage msg = null;
        int funCode = -1;
        if (messageType == MessageZj.class) {
            if (direction.equals(IMessage.DIRECTION_UP)) {
                msg = MessageZj.loadRepMessage(rawData); 
                funCode = ((MessageZj) msg).headIn.c_func;
            }
            else {
                msg = MessageZj.loadReqMessage(rawData);
                funCode = ((MessageZj) msg).headOut.c_func;
            }
        }
        
        if (msg != null) {
            MessageDecoder decoder = getDecoder(messageType, funCode);
            return decoder.decode(msg);
        }
        else {
            return null;
        }
    }*/
    
    
    /**
     * ����Ϣ������ַ�����ʽ
     * @param message ��Ϣ
     * @return ��ʾ��Ϣ���ַ���
     */
    public static String marshal(IMessage message) {
        MessageDecoder decoder = getDecoder(message.getClass(), FunctionCode.SERIALIZE_MESSAGE);
        return (String) decoder.decode(message);
    }
    
    /**
     * ���ַ�����ԭ��Ϣ
     * @param s ��Ϣ���ַ�����ʾ
     * @param messageType ��Ϣ����
     * @return ��Ϣ
     */
    public static IMessage unmarshal(String s, Class messageType) {
        MessageEncoder encoder = getEncoder(messageType, FunctionCode.SERIALIZE_MESSAGE);
        return encoder.encode(s)[0];
    }
    
    /**
     * ȡ����Ӧ�ı�����
     * @param messageType ��Ϣ����
     * @param funCode ������
     * @return ƥ��ı���������û��ƥ��ı��������򷵻� null
     */
    private static MessageEncoder getEncoder(Class messageType, int funCode) {
        ProtocolHandler handler = handlerFactory.getProtocolHandler(messageType);
        return handler.getCodecFactory().getEncoder(funCode);
    }
    
    /**
     * ȡ����Ӧ�Ľ�����
     * @param messageType ��Ϣ����
     * @param funCode ������
     * @return ƥ��Ľ���������û��ƥ��Ľ��������򷵻� null
     */
    private static MessageDecoder getDecoder(Class messageType, int funCode) {
        ProtocolHandler handler = handlerFactory.getProtocolHandler(messageType);
        return handler.getCodecFactory().getDecoder(funCode);
    }
}
