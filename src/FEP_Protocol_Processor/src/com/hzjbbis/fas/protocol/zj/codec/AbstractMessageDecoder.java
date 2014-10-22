package com.hzjbbis.fas.protocol.zj.codec;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.codec.MessageDecoder;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;

/**
 * �㽭��Լ��Ϣ������������
 * @author ������
 */
public abstract class AbstractMessageDecoder implements MessageDecoder {

    /** Э���������� */
    protected ProtocolDataConfig dataConfig;
    
    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageDecoder#setDataConfig(com.hzjbbis.fas.protocol.conf.ProtocolDataConfig)
     */
    public void setDataConfig(ProtocolDataConfig dataConfig) {
        this.dataConfig = dataConfig;
    }
    
    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageDecoder#decode(com.hzjbbis.fas.framework.IMessage)
     */
    public abstract Object decode(IMessage message);
}
