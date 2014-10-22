package com.hzjbbis.exception;

/**
 * 业务处理异常
 * @author 张文亮
 */
public class BusinessProcessException extends RuntimeException {

    private static final long serialVersionUID = 4188947643314274911L;

    public BusinessProcessException() {
        super();
    }

    /**
     * @param message
     */
    public BusinessProcessException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public BusinessProcessException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public BusinessProcessException(String message, Throwable cause) {
        super(message, cause);
    }

}
