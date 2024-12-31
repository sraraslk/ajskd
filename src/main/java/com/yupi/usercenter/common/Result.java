package com.yupi.usercenter.common;

public class Result {
    /**
     * return 成功
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(0,data,"ok","");
    }

    /**
     * 返回失败
     * @param errorCode
     * @return
     */
    public static  BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }
    /**
     * 返回失败
     * @param
     * @return
     */
    public static  BaseResponse error(int code,String message,String description) {
        return new BaseResponse<>(code,null,message,description);
    }
    /**
     * 返回失败
     * @param errorCode
     * @return
     */
    public static  BaseResponse error(ErrorCode errorCode,String message,String description) {
        return new BaseResponse<>(errorCode.getCode(),null,message,description);
    }
    /**
     * 返回失败
     * @param errorCode
     * @return
     */
    public static  BaseResponse error(ErrorCode errorCode,String description) {
        return new BaseResponse<>(errorCode.getCode(),null, errorCode.getMsg(),description);
    }
}
