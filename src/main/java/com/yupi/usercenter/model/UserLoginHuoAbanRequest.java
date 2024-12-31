package com.yupi.usercenter.model;

import lombok.Data;

@Data
public class UserLoginHuoAbanRequest {
    private String userAccount;
    private String userPassword;
}
