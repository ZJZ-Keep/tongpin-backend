package com.zjz.tongpin.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息
 */
@TableName(value = "message")
@Data
public class Message implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者 id
     */
    private Long senderId;

    /**
     * 接收者 id
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 0 - 未读，1 - 已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
