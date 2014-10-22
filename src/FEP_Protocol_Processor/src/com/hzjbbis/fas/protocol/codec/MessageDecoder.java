package com.hzjbbis.fas.protocol.codec;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;

/**
 * ��Ϣ���������ѿ�����ת����������Ϣ�����ҵ�����
 * @author ������
 */
public interface MessageDecoder {

    /**
     * ����Э����������
     * @param dataConfig
     */
    public void setDataConfig(ProtocolDataConfig dataConfig);
    
    /**
     * ����Ϣ����ɸ��߼���ҵ�����
     * @param message ��Ϣ
     * @return ҵ�����
     */
    public Object decode(IMessage message);
}
