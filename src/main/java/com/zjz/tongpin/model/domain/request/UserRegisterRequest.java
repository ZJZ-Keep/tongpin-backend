package com.zjz.tongpin.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 *   *   */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;

    /**
     * 星球编号
     */
    private String planetCode;
}

// [导航](https:// zjz.icu) 入门捷径+交流答疑+项目实战+求职指导，帮你自学不走弯路