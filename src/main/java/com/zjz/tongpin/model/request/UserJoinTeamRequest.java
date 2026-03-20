package com.zjz.tongpin.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserJoinTeamRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 队伍id
     */
    private Long TeamId;

    /**
     * 密码
     */
    private String password;

}
