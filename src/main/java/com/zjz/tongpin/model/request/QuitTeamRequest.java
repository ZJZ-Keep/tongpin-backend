package com.zjz.tongpin.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuitTeamRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 队伍id
     */
    private Long teamId;


}
