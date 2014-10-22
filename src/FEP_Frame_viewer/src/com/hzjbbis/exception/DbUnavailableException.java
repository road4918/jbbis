package com.hzjbbis.exception;

/**
 * 数据库不可用异常
 * @author 张文亮
 */
public class DbUnavailableException extends RuntimeException {

    private static final long serialVersionUID = -209993941272187585L;

    public DbUnavailableException() {
        super();
    }

    /**
     * @param message
     */
    public DbUnavailableException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public DbUnavailableException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public DbUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
