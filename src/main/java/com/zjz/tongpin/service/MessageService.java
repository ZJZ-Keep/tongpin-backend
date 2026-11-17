package com.zjz.tongpin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjz.tongpin.model.domain.Message;
import com.zjz.tongpin.model.vo.MessageVo;

import java.util.List;

/**
 * 聊天消息服务
 */
public interface MessageService extends IService<Message> {

    /**
     * 获取与某人的历史消息（按时间正序）
     */
    List<Message> getHistory(Long userId, Long targetId, long pageNum, long pageSize);

    /**
     * 获取会话列表
     */
    List<MessageVo> getConversations(Long userId);

    /**
     * 标记与某人的消息为已读
     */
    boolean markRead(Long userId, Long targetId);

    /**
     * 统计未读消息数
     */
    long countUnread(Long userId);

    /**
     * 保存一条消息
     */
    Message saveMessage(Long senderId, Long receiverId, String content);
}
