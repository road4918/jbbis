package com.hzjbbis.exception;

/**
 * ҵ�����쳣
 * @author ������
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
