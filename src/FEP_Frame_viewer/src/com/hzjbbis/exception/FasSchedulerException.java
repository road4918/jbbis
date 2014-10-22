package com.hzjbbis.exception;

/**
 * 日程调度异常
 * @author 张文亮
 */
public class FasSchedulerException extends RuntimeException {

    private static final long serialVersionUID = 1833978439391598200L;

    public FasSchedulerException() {
        super();
    }

    /**
     * @param message
     */
    public FasSchedulerException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public FasSchedulerException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public FasSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }

}
