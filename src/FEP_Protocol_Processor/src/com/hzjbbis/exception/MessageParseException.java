package com.hzjbbis.exception;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.hzjbbis.util.HexDump;

/**
 * <p>Title: Java Socket Server with NIO support </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author bhw
 * @version 1.0
 */

public class MessageParseException extends IOException {
	private static final long serialVersionUID = 200603141603L;

	private ByteBuffer buffer;

	/**
	 * Constructs a new instance with the specified message and the specified
	 * cause.
	 */
	public MessageParseException(String message, ByteBuffer buff) {
		super(message);
		if (null == buff)
			return;
		if (buff.position() > 0) //�Ѿ���ȡ��һЩ����
			buff.rewind();
		buffer = buff.slice();
	}

	/**
	 * Returns the message and the hexdump of the unknown part.
	 */
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
}