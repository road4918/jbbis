/**
 * 对于需要把消息序列化到缓冲的消息，需要实现消息的序列化成字符串以及从字符串加载为消息的接口。
 */
package com.hzjbbis.fk.message;

import com.hzjbbis.fk.message.IMessage;

/**
 * @author hbao
 * 2008-06-14 10:04
 */
public interface MessageLoader {
	IMessage loadMessage(String serializedString);
	String 	serializeMessage(IMessage message);
}
