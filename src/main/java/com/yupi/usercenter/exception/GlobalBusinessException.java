package com.yupi.usercenter.exception;

import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalBusinessException {


    @ExceptionHandler(WxException.class)
    public BaseResponse wxExceptionHandler(WxException e) {
        log.error("WxException",e.getMessage(), e);
        return Result.error(e.getCode(),e.getMessage(),e.getDescription());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse RuntimeExceptionHandler(Exception e) {
        log.error("RuntimeException", e);
        return Result.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");

    }
}
