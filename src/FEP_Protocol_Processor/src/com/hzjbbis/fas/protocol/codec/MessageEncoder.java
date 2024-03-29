package com.hzjbbis.fas.protocol.codec;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.protocol.conf.ProtocolDataConfig;

/**
 * 消息编码器。把业务对象编码成适合于控制器处理的消息对象
 * @author 张文亮
 */
public interface MessageEncoder {

    /**
     * 设置协议数据配置
     * @param dataConfig
     */
    public void setDataConfig(ProtocolDataConfig dataConfig);

    /**
     * 把业务对象编码成适合于控制器处理的消息对象
     * @param obj 业务对象
     * @return 消息对象数组
     */
    public IMessage[] encode(Object obj);
}
