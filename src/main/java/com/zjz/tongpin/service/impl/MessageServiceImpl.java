package com.zjz.tongpin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjz.tongpin.mapper.MessageMapper;
import com.zjz.tongpin.model.domain.Message;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.model.vo.MessageVo;
import com.zjz.tongpin.model.vo.UserVo;
import com.zjz.tongpin.service.MessageService;
import com.zjz.tongpin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 聊天消息服务实现
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private UserService userService;

    @Override
    public List<Message> getHistory(Long userId, Long targetId, long pageNum, long pageSize) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w
                .and(x -> x.eq("senderId", userId).eq("receiverId", targetId))
                .or(x -> x.eq("senderId", targetId).eq("receiverId", userId)));
        wrapper.orderByDesc("createTime");
        Page<Message> page = new Page<>(pageNum, pageSize);
        Page<Message> result = this.page(page, wrapper);
        List<Message> records = result.getRecords();
        // 数据库按时间倒序，翻转为正序展示
        Collections.reverse(records);
        return records;
    }

    @Override
    public List<MessageVo> getConversations(Long userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("senderId", userId).or().eq("receiverId", userId));
        wrapper.orderByDesc("createTime");
        List<Message> messages = this.list(wrapper);

        // 未读数统计
        Map<Long, Long> unreadCountMap = new HashMap<>();
        QueryWrapper<Message> unreadWrapper = new QueryWrapper<>();
        unreadWrapper.select("senderId", "COUNT(*) as cnt");
        unreadWrapper.eq("receiverId", userId).eq("isRead", 0);
        unreadWrapper.groupBy("senderId");
        List<Map<String, Object>> unreadMaps = this.listMaps(unreadWrapper);
        for (Map<String, Object> map : unreadMaps) {
            Object senderIdObj = map.get("senderId");
            Object cntObj = map.get("cnt");
            if (senderIdObj != null && cntObj != null) {
                unreadCountMap.put(((Number) senderIdObj).longValue(), ((Number) cntObj).longValue());
            }
        }

        // 每个联系人只保留最新一条
        Set<Long> seen = new HashSet<>();
        List<MessageVo> result = new ArrayList<>();
        List<Long> targetIds = new ArrayList<>();
        for (Message message : messages) {
            Long otherId = Objects.equals(message.getSenderId(), userId)
                    ? message.getReceiverId() : message.getSenderId();
            if (otherId == null || !seen.add(otherId)) {
                continue;
            }
            MessageVo vo = new MessageVo();
            vo.setTargetId(otherId);
            vo.setLastMessage(message.getContent());
            vo.setLastTime(message.getCreateTime());
            vo.setUnreadCount(unreadCountMap.getOrDefault(otherId, 0L));
            result.add(vo);
            targetIds.add(otherId);
        }

        // 批量补充对方用户信息
        if (!targetIds.isEmpty()) {
            List<User> users = userService.listByIds(targetIds);
            Map<Long, UserVo> userVoMap = new HashMap<>();
            for (User user : users) {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                userVoMap.put(user.getId(), userVo);
            }
            for (MessageVo vo : result) {
                vo.setTargetUser(userVoMap.get(vo.getTargetId()));
            }
        }
        return result;
    }

    @Override
    public boolean markRead(Long userId, Long targetId) {
        UpdateWrapper<Message> wrapper = new UpdateWrapper<>();
        wrapper.eq("receiverId", userId).eq("senderId", targetId).eq("isRead", 0);
        wrapper.set("isRead", 1);
        return this.update(wrapper);
    }

    @Override
    public long countUnread(Long userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiverId", userId).eq("isRead", 0);
        return this.count(wrapper);
    }

    @Override
    public Message saveMessage(Long senderId, Long receiverId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreateTime(new Date());
        this.save(message);
        return message;
    }
}
