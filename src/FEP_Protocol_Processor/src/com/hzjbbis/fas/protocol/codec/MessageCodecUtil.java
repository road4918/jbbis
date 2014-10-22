package com.hzjbbis.fas.protocol.codec;

import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.handler.ProtocolHandlerFactory;
import com.hzjbbis.fas.protocol.zj.FunctionCode;

/**
 * 消息编码/解码工具。提供几个常规的编码/解码方法
 * @author 张文亮
 */
public abstract class MessageCodecUtil {

    /** 协议处理器工厂 */
    private static final ProtocolHandlerFactory handlerFactory = ProtocolHandlerFactory.getInstance();    

    /**
     * 将消息解码成更高级的业务对象
     * @param rawData 消息的原始报文
     * @param direction 消息传输方向
     * @param messageType 消息类型
     * @return 业务对象
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
     * 把消息打包成字符串格式
     * @param message 消息
     * @return 表示消息的字符串
     */
    public static String marshal(IMessage message) {
        MessageDecoder decoder = getDecoder(message.getClass(), FunctionCode.SERIALIZE_MESSAGE);
        return (String) decoder.decode(message);
    }
    
    /**
     * 从字符串还原消息
     * @param s 消息的字符串表示
     * @param messageType 消息类型
     * @return 消息
     */
    public static IMessage unmarshal(String s, Class messageType) {
        MessageEncoder encoder = getEncoder(messageType, FunctionCode.SERIALIZE_MESSAGE);
        return encoder.encode(s)[0];
    }
    
    /**
     * 取得相应的编码器
     * @param messageType 消息类型
     * @param funCode 功能码
     * @return 匹配的编码器。若没有匹配的编码器，则返回 null
     */
    private static MessageEncoder getEncoder(Class messageType, int funCode) {
        ProtocolHandler handler = handlerFactory.getProtocolHandler(messageType);
        return handler.getCodecFactory().getEncoder(funCode);
    }
    
    /**
     * 取得相应的解码器
     * @param messageType 消息类型
     * @param funCode 功能码
     * @return 匹配的解码器。若没有匹配的解码器，则返回 null
     */
    private static MessageDecoder getDecoder(Class messageType, int funCode) {
        ProtocolHandler handler = handlerFactory.getProtocolHandler(messageType);
        return handler.getCodecFactory().getDecoder(funCode);
    }
}
