package com.hzjbbis.exception;

/**
 * ��¼�ظ��쳣
 * @author ������
 */
public class DuplicatedRecordException extends RuntimeException {

    private static final long serialVersionUID = -3093670693617575889L;

    public DuplicatedRecordException() {
        super();
    }

    /**
     * @param message
     */
    public DuplicatedRecordException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public DuplicatedRecordException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public DuplicatedRecordException(String message, Throwable cause) {
        super(message, cause);
    }

}
