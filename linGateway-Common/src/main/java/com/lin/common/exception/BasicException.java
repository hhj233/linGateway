package com.lin.common.exception;

import com.lin.common.enums.ResponseCode;

/**
 * @author linzj
 */
public class BasicException extends RuntimeException{
    public BasicException(){}
    protected ResponseCode code;

    public BasicException(String message, ResponseCode code) {
        super(message);
        this.code = code;
    }

    public BasicException(String message, Throwable cause, ResponseCode code) {
        super(message, cause);
        this.code = code;
    }

    public BasicException(Throwable cause, ResponseCode code) {
        super(cause);
        this.code = code;
    }

    public BasicException(String message, Throwable cause, boolean enableSuppression,
                          boolean writableStackTrace, ResponseCode code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public ResponseCode getCode() {
        return this.code;
    }
}
