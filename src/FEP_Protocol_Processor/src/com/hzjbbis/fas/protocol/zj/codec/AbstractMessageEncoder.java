package com.hzjbbis.fas.protocol.zj.codec;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.codec.MessageEncoder;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;

/**
 * �㽭��Լ��Ϣ������������
 * @author ������
 */
public abstract class AbstractMessageEncoder implements MessageEncoder {

    /** Э���������� */
    protected ProtocolDataConfig dataConfig;

    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageEncoder#setDataConfig(com.hzjbbis.fas.protocol.conf.ProtocolDataConfig)
     */
    public void setDataConfig(ProtocolDataConfig dataConfig) {
        this.dataConfig = dataConfig;
    }
    
    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageEncoder#encode(java.lang.Object)
     */
    public abstract IMessage[] encode(Object obj);
}
