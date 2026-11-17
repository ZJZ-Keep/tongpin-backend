package com.zjz.tongpin.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * WebSocket 聊天消息协议体
 */
@Data
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型：chat / ping
     */
    private String type;

    /**
     * 接收者 id
     */
    private Long toUserId;

    /**
     * 消息内容
     */
    private String content;
}
