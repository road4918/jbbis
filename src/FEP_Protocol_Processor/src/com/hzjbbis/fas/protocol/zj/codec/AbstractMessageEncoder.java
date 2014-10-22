package com.hzjbbis.fas.protocol.zj.codec;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.codec.MessageEncoder;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;

/**
 * 浙江规约消息编码器抽象类
 * @author 张文亮
 */
public abstract class AbstractMessageEncoder implements MessageEncoder {

    /** 协议数据配置 */
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
