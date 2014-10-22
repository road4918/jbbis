package com.hzjbbis.fas.protocol.handler;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.model.FaalRequest;
import com.hzjbbis.fas.protocol.codec.MessageCodecFactory;

/**
 * Э�鴦�������������ض�Э�����Ϣ
 * @author ������
 */
public interface ProtocolHandler {
    
    /**
     * ���ñ���/����������
     * @param codecFactory ����/����������
     */
    public void setCodecFactory(MessageCodecFactory codecFactory);
    
    /**
     * ȡ�ñ���/����������
     * @return ����/����������
     */
    public MessageCodecFactory getCodecFactory();
    
    /**
     * ������Ϣ
     * @param message ��Ϣ
     * @return Ӧ����Ϣ���������ҪӦ���򷵻� null
     */
    public Object process(IMessage message);
    
    /**
     * ��װ��Ϣ
     * @param request ͨѶ����
     * @return �����ض���Լ����Ϣ����
     */
    public IMessage[] createMessage(FaalRequest request);
}
