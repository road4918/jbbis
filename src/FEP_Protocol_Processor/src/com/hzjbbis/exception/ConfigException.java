package com.hzjbbis.exception;

/**
 * ≈‰÷√“Ï≥£
 * @author ’≈Œƒ¡¡
 */
public class ConfigException extends RuntimeException {

    private static final long serialVersionUID = -8071053409130303967L;

    public ConfigException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ConfigException(Throwable cause) {
        super(cause);
    }

}
