package com.example.interact.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author W.G.
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -543286974259314538L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String vipCode;
}
