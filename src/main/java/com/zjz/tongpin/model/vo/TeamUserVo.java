package com.zjz.tongpin.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 队伍和用户关联表（用户加入的队伍）
 * @TableName team
 */
@Data
public class TeamUserVo {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     *创建人用户信息
     */
    private UserVo createUserVo;

    /**
     * 已经加入的队伍数
     */
    private Number hasJoinNum;

    /**
     * 是否已经加入队伍
     */
    private boolean hasJoin=false;
}