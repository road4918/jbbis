package com.hzjbbis.fas.protocol.conf;

import java.util.List;

/**
 * Э���ṩ������
 * @author ������
 */
public class ProtocolProviderConfig {

    /** Э�鴦�����б� */
    private List handlers;
    
    /**
     * ȡ��Э�鴦��������
     * @param messageType ��Ϣ����
     * @return Э�鴦��������
     */
    public ProtocolHandlerConfig getProtocolHandlerConfig(String messageType) {
        if (handlers == null) {
            return null;
        }
        
        for (int i = 0; i < handlers.size(); i++) {
            ProtocolHandlerConfig handler = (ProtocolHandlerConfig) handlers.get(i);
            if (handler.getMessageType().equals(messageType)) {
                return handler;
            }
        }
        
        return null;
    }

    /**
     * @return Returns the handlers.
     */
    public List getHandlers() {
        return handlers;
    }

    /**
     * @param handlers The handlers to set.
     */
    public void setHandlers(List handlers) {
        this.handlers = handlers;
    } 
}
