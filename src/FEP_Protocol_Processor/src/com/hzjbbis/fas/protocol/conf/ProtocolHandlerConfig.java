package com.hzjbbis.fas.protocol.conf;

/**
 * Э�鴦��������
 * @author ������
 */
public class ProtocolHandlerConfig {

    /** �������Ϣ���� */
    private String messageType;
    /** Э�鴦������ʵ������ */
    private String handlerClass;
    /** Э�鴦����ʹ�õı���/���������� */
    private CodecFactoryConfig codecFactory;
        
    /**
     * @return Returns the messageType.
     */
    public String getMessageType() {
        return messageType;
    }
    /**
     * @param messageType The messageType to set.
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    /**
     * @return Returns the handlerClass.
     */
    public String getHandlerClass() {
        return handlerClass;
    }
    /**
     * @param handlerClass The handlerClass to set.
     */
    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }
    /**
     * @return Returns the codecFactory.
     */
    public CodecFactoryConfig getCodecFactory() {
        return codecFactory;
    }
    /**
     * @param codecFactory The codecFactory to set.
     */
    public void setCodecFactory(CodecFactoryConfig codecFactory) {
        this.codecFactory = codecFactory;
    }
}
