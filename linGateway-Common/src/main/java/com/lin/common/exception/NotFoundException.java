package com.lin.common.exception;

import com.lin.common.enums.ResponseCode;

/**
 * @author linzj
 */
public class NotFoundException extends BasicException{

    public NotFoundException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public NotFoundException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
    }
}
