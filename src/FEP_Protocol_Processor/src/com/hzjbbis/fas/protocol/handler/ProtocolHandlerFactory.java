package com.hzjbbis.fas.protocol.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hzjbbis.fas.protocol.codec.MessageCodecFactory;
import com.hzjbbis.fas.protocol.conf.CodecFactoryConfig;
import com.hzjbbis.fas.protocol.conf.ProtocolHandlerConfig;
import com.hzjbbis.fas.protocol.conf.ProtocolProviderConfig;
import com.hzjbbis.fas.protocol.meter.MeterProtocolFactory;
import com.hzjbbis.util.CastorUtil;

/**
 * Э�鴦�������������Ը���Э������ṩ���ʵ�Э�鴦����
 * @author ������
 */
public class ProtocolHandlerFactory {

    /** ����ӳ���ļ� */
    private static final String CONFIG_MAPPING = "/com/hzjbbis/fas/protocol/conf/protocol-provider-config-mapping.xml";
    /** �����ļ� */
    private static final String CONFIG_RESOURCE = "/com/hzjbbis/fas/protocol/conf/protocol-provider-config.xml";
    
    /** ���� */
    private static ProtocolHandlerFactory instance;
    
    /** Э�鴦�����б� */
    private Map handlers = new HashMap();
    
    /**
     * ����һ��Э�鴦��������
     *
     */
    private ProtocolHandlerFactory() {
        init();
    }
    
    /**
     * �õ�һ��Э�鴦����������ʵ��
     * @return Э�鴦��������
     */
    public static ProtocolHandlerFactory getInstance() {
        if (instance == null) {
            synchronized (ProtocolHandlerFactory.class) {
                if (instance == null) {
                    instance = new ProtocolHandlerFactory();
                }
            }
        }
        return instance;
    }
    
    /**
     * ȡ���ʺ��ڴ����ض������Ϣ��Э�鴦����
     * @param messageType ��Ϣ����
     * @return Э�鴦����
     */
    public ProtocolHandler getProtocolHandler(Class messageType) {
        return (ProtocolHandler) handlers.get(messageType.getName());
    }
    
    /**
     * ��ʼ��Э�鴦��������
     */
    private void init() {
        ProtocolProviderConfig config = (ProtocolProviderConfig) CastorUtil.unmarshal(
                CONFIG_MAPPING, CONFIG_RESOURCE);
        List handlerConfigs = config.getHandlers();
        for (int i = 0; i < handlerConfigs.size(); i++) {
            ProtocolHandlerConfig handlerConfig = (ProtocolHandlerConfig) handlerConfigs.get(i);
            ProtocolHandler handler = (ProtocolHandler) newInstance(handlerConfig.getHandlerClass());            
            CodecFactoryConfig codecFactoryConfig = handlerConfig.getCodecFactory();
            if (codecFactoryConfig != null) {
                MessageCodecFactory codecFactory = (MessageCodecFactory) newInstance(
                        codecFactoryConfig.getFactoryClass());
                codecFactory.setConfig(codecFactoryConfig);
                handler.setCodecFactory(codecFactory);
            }
            
            handlers.put(handlerConfig.getMessageType(), handler);
        }
        
        //by yangdh ----init meter protocol
        MeterProtocolFactory.createMeterProtocolDataSet("ZJMeter");
        MeterProtocolFactory.createMeterProtocolDataSet("BBMeter");
    }
    
    /**
     * ����һ��ʵ��
     * @param className ����
     * @return ���ʵ��
     */
    private Object newInstance(String className) {
        try {
            Class clazz = Class.forName(className);
            return clazz.newInstance();
        }
        catch (Exception ex) {
            throw new RuntimeException("Error to instantiating class: " + className, ex);
        }
    }
}
