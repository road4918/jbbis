package com.hzjbbis.fas.protocol.codec;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;

/**
 * ��Ϣ����������ҵ����������ʺ��ڿ������������Ϣ����
 * @author ������
 */
public interface MessageEncoder {

    /**
     * ����Э����������
     * @param dataConfig
     */
    public void setDataConfig(ProtocolDataConfig dataConfig);

    /**
     * ��ҵ����������ʺ��ڿ������������Ϣ����
     * @param obj ҵ�����
     * @return ��Ϣ��������
     */
    public IMessage[] encode(Object obj);
}
