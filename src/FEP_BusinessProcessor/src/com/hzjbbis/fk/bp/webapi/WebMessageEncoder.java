package com.hzjbbis.fk.bp.webapi;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.hzjbbis.exception.MessageEncodeException;
import com.hzjbbis.fas.model.FaalRequest;

/**
 * WebͨѶ��Ϣ������
 * @author ������
 */
public class WebMessageEncoder {

    /**
     * ��һ�� Java �������� WebMessage ����
     * @param obj Java ����
     * @return WebMessage ����
     */
    public MessageWeb encode(Object obj) {
        assert obj != null;
        MessageWeb msg = new MessageWeb();
        if (obj instanceof FaalRequest) {
            FaalRequest request = (FaalRequest) obj;
            msg.getHead().setCommand(request.getType());
        }
        else {
            msg.getHead().setCommand(FaalRequest.TYPE_OTHER);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutputStream serializer = new ObjectOutputStream(out);
            serializer.writeObject(obj);
            byte[] buf = out.toByteArray();
            msg.getHead().setIntBodylen(buf.length);
            msg.setData( ByteBuffer.wrap(buf) );
        }
        catch (Exception ex) {
            throw new MessageEncodeException("Error to encode object to web message: " + obj, ex);
        }
        finally {
            // ���� ByteArrayOutputStream, ���� close() û���κ�Ч��
        }
        
        return msg;
    }
}
