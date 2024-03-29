package com.hzjbbis.fas.protocol.codec;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;

/**
 * 消息解码器。把控制器转发过来的消息解码成业务对象
 * @author 张文亮
 */
public interface MessageDecoder {

    /**
     * 设置协议数据配置
     * @param dataConfig
     */
    public void setDataConfig(ProtocolDataConfig dataConfig);
    
    /**
     * 将消息解码成更高级的业务对象
     * @param message 消息
     * @return 业务对象
     */
    public Object decode(IMessage message);
}
