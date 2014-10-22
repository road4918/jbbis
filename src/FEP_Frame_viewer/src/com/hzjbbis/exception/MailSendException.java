package com.hzjbbis.exception;

/**
 * �ʼ������쳣
 * @author ������
 */
public class MailSendException extends RuntimeException {

    private static final long serialVersionUID = -8871657727639590254L;

    public MailSendException() {
        super();
    }

    /**
     * @param message
     */
    public MailSendException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MailSendException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }

}
