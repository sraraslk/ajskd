package com.yupi.usercenter.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class SaveMinioRequest implements Serializable {


    private String id;
    private String url;

}
