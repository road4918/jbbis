package com.hzjbbis.fas.protocol.codec;

import com.hzjbbis.fas.protocol.conf.CodecFactoryConfig;

/**
 * 消息编码/解码器工厂
 * @author 张文亮
 */
public interface MessageCodecFactory {

    /**
     * 设置编码/解码器工厂配置
     * @param dataConfig 配置对象
     */
    public void setConfig(CodecFactoryConfig config);
        
    /**
     * 获得适合于处理特定消息的编码器
     * @param funCode 功能码
     * @return 编码器
     */
    public MessageEncoder getEncoder(int funCode);
    
    /**
     * 获得适合于处理特定消息的解码器
     * @param funCode 功能码
     * @return 解码器
     */
    public MessageDecoder getDecoder(int funCode);
}
