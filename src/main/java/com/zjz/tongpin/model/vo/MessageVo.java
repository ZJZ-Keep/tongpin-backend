package com.zjz.tongpin.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 会话（聊天列表）展示对象
 */
@Data
public class MessageVo {

    /**
     * 对方用户 id
     */
    private Long targetId;

    /**
     * 对方用户信息
     */
    private UserVo targetUser;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后一条消息时间
     */
    private Date lastTime;

    /**
     * 未读消息数
     */
    private long unreadCount;
}
