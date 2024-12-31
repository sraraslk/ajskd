package com.yupi.usercenter.model;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserRegisterRequest implements Serializable {


    private String userAccount;
    private String userPassword;
    public String CheckPassword;
    private String planetCode;
}
