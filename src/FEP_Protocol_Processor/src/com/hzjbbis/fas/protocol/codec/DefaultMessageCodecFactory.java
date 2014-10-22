package com.hzjbbis.fas.protocol.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.conf.CodecConfig;
import com.hzjbbis.fas.protocol.conf.CodecFactoryConfig;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;
import com.hzjbbis.util.CastorUtil;

/**
 * ��Ϣ����/������������ȱʡʵ��
 * @author ������
 */
public class DefaultMessageCodecFactory implements MessageCodecFactory {
	private static final Log log=LogFactory.getLog(DefaultMessageCodecFactory.class);
    /** Э������������ */
    private ProtocolDataConfig dataConfig;
    /** �������б� */
    private Map encoders;
    /** �������б� */
    private Map decoders;
    
    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageCodecFactory#setConfig(com.hzjbbis.fas.protocol.conf.CodecFactoryConfig)
     */
    public void setConfig(CodecFactoryConfig config) {
        init(config);
    }

    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageCodecFactory#getEncoder(int)
     */
    public MessageEncoder getEncoder(int funCode) {
        return (MessageEncoder) encoders.get(new Integer(funCode));
    }

    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageCodecFactory#getDecoder(int)
     */
    public MessageDecoder getDecoder(int funCode) {
        return (MessageDecoder) decoders.get(new Integer(funCode));
    }

    /**
     * ��ʼ��
     * @param config 
     */
    private void init(CodecFactoryConfig config) {
        try{
	    	dataConfig = (ProtocolDataConfig) CastorUtil.unmarshal(
	                config.getDataConfigMapping(), config.getDataConfigResource());
	        dataConfig.fillMap();
	        encoders = new HashMap();
	        decoders = new HashMap();
	        List codecConfigs = config.getCodecs();
	        if(codecConfigs!=null){
		        for (int i = 0; i < codecConfigs.size(); i++) {
		            CodecConfig codecConfig = (CodecConfig) codecConfigs.get(i);
		            Integer funCode = new Integer(codecConfig.getFunCode());
		            
		            if (codecConfig.getEncoderClass() != null) {
		                MessageEncoder encoder = (MessageEncoder) newInstance(
		                        codecConfig.getEncoderClass());
		                encoder.setDataConfig(dataConfig);
		                encoders.put(funCode, encoder);
		            }
		            
		            if (codecConfig.getDecoderClass() != null) {
		                MessageDecoder decoder = (MessageDecoder) newInstance(
		                        codecConfig.getDecoderClass());
		                decoder.setDataConfig(dataConfig);
		                decoders.put(funCode, decoder);
		            }
		        }
	        }
        }catch(Exception e){
        	log.error("load protocol setting",e);
        }
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
