package com.yupi.usercenter.model;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String userAccount;
    private String userPassword;
    private String captchaKey;
    private String code;
}
