/**
 * ��Ϣ�����쳣
 */
package com.hzjbbis.fk.exception;

import java.nio.ByteBuffer;

import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bao
 *
 */
public class MessageParseException extends Exception {
	private static final long serialVersionUID = -5985134647725926736L;
	private ByteBuffer buffer;
	
    public MessageParseException(String message)
    {
        super(message);
    }
    
	public MessageParseException(String message, ByteBuffer buff) {
		super(message);
		if (null == buff)
			return;
		if (buff.position() > 0) //�Ѿ���ȡ��һЩ����
			buff.rewind();
		buffer = buff.slice();
	}
	
	public String getMessage() {
		String message = super.getMessage();

		if (message == null) {
			message = "";
		}

		if (buffer != null) {
			return message + ((message.length() > 0) ? " " : "") + "(Hexdump: "
					+ HexDump.hexDump(buffer) + ')';
		} else {
			return message;
		}
	}

    public String getLocalizedMessage()
    {
        return getMessage();
    }
}
