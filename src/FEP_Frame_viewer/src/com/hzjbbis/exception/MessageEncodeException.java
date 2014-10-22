package com.hzjbbis.exception;

/**
 * 消息编码异常
 * @author 张文亮
 */
public class MessageEncodeException extends RuntimeException {
	private int errcode;
	private int funccode;
	
    private static final long serialVersionUID = -2488563605733447133L;

    public MessageEncodeException() {
        super();
    }

    /**
     * @param message
     */
    public MessageEncodeException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MessageEncodeException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public MessageEncodeException(String message, Throwable cause) {
        super(message, cause);
    }

	public int getErrcode() {
		return errcode;
	}

	public void setErrcode(int errcode) {
		this.errcode = errcode;
	}
    
    
}
