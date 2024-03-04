package com.lin.common.exception;

import com.lin.common.enums.ResponseCode;

/**
 * @author linzj
 */
public class ResponseException extends BasicException{
    public ResponseException() {
        this(ResponseCode.INTERNAL_ERROR);
    }
    public ResponseException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public ResponseException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
    }
}
