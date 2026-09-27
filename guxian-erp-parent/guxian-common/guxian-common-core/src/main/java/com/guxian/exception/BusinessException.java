package com.guxian.exception;

import lombok.Data;

/**
 * 业务运行时异常，用于业务主动抛出异常
 */
@Data
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}