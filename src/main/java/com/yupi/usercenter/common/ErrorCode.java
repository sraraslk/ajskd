package com.yupi.usercenter.common;

/**
 * 错误码
 */
public enum ErrorCode {
    /**
     * 成功
     */
    SUCCESS(0, "ok", "成功！"),
    /**
     * 请求参数错误
     */
    PARAMS_ERROR(40000, "请求参数错误", ""),
    /**
     * 请求数据为空
     */
    NULL_ERROR(40001, "请求数据为空", ""),
    /**
     * 未登录
     */
    NOT_LOGIN(40100, "未登录", ""),
    /**
     * 无权限
     */
    NO_AUTH(40101, "无权限", ""),
    /**
     * 系统内部异常
     */
    SYSTEM_ERROR(50000,"系统内部异常","");




    private final int code;
    private final String msg;
    private final String description;

    ErrorCode(int code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getMsg() {
        return msg;
    }
}
