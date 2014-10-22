/**
 * ��Socket������ʱ���������IOException�����ʾ�Է��ر�TCP���ӣ���Ҫ�׳�SocketClientCloseException��
 * 
 */
package com.hzjbbis.fk.exception;

import java.io.IOException;

/**
 * @author bao
 *
 */
public class SocketClientCloseException extends RuntimeException {
	private static final long serialVersionUID = 6187628305543356505L;
	
	public SocketClientCloseException(IOException e){
		super(e);
	}
	
	public SocketClientCloseException(String message){
		super(message);
	}
}
