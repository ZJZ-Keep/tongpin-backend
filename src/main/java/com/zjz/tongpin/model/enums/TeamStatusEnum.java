package com.zjz.tongpin.model.enums;

import lombok.Getter;
import lombok.Setter;

public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    @Setter
    @Getter
    private int status;

    private String text;

    public static TeamStatusEnum teamStatusEnum(Integer status){
        if (status==null){
            return null;
        }
        TeamStatusEnum[] teamStatusEnums = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : teamStatusEnums) {
            if (status==teamStatusEnum.getStatus()){
                return teamStatusEnum;
            }
        }
        return null;

    }

    TeamStatusEnum(int status, String text) {
        this.status = status;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setPassword(String text) {
        this.text = text;
    }

    }
