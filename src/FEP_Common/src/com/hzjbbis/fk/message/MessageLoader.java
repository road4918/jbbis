/**
 * ������Ҫ����Ϣ���л����������Ϣ����Ҫʵ����Ϣ�����л����ַ����Լ����ַ�������Ϊ��Ϣ�Ľӿڡ�
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
