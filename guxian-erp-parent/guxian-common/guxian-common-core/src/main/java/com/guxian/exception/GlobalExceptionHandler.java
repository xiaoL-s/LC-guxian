package com.guxian.exception;

import com.guxian.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常捕获切面，所有Controller异常统一拦截返回JSON
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("【业务异常】code:{}, msg:{}", e.getCode(), e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 @Valid
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String errMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("【参数校验异常】{}", errMsg);
        return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), errMsg);
    }

    /**
     * 兜底捕获所有未知系统异常。
     * 返回精简后的异常信息（截断），便于前端定位问题、用户反馈，避免"系统繁忙"黑盒。
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("【系统未知异常】", e);
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = e.getClass().getSimpleName();
        }
        if (msg.length() > 120) {
            msg = msg.substring(0, 120) + "...";
        }
        return Result.fail(ResultCodeEnum.FAIL.getCode(), "系统异常：" + msg);
    }
}