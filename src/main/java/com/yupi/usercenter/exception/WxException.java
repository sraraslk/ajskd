package com.yupi.usercenter.exception;

import com.yupi.usercenter.common.ErrorCode;

public class WxException extends RuntimeException {

    private final int code;

    private final String description;

    public WxException(int code,String message,  String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public WxException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }
    public WxException(ErrorCode errorCode, String description) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }
}
