package com.hzjbbis.fas.protocol.codec;

import com.hzjbbis.fas.protocol.conf.CodecFactoryConfig;

/**
 * ��Ϣ����/����������
 * @author ������
 */
public interface MessageCodecFactory {

    /**
     * ���ñ���/��������������
     * @param dataConfig ���ö���
     */
    public void setConfig(CodecFactoryConfig config);
        
    /**
     * ����ʺ��ڴ����ض���Ϣ�ı�����
     * @param funCode ������
     * @return ������
     */
    public MessageEncoder getEncoder(int funCode);
    
    /**
     * ����ʺ��ڴ����ض���Ϣ�Ľ�����
     * @param funCode ������
     * @return ������
     */
    public MessageDecoder getDecoder(int funCode);
}
