package com.zjz.tongpin.handler;

import com.google.gson.Gson;
import com.zjz.tongpin.model.domain.Message;
import com.zjz.tongpin.model.request.ChatMessage;
import com.zjz.tongpin.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天 WebSocket 处理器
 * 维护 userId -> WebSocketSession，实现 1 对 1 私聊消息转发与落库
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /**
     * 在线用户连接：userIds -> sessions
     */
    private static final Map<Long, Set<WebSocketSession>> USER_SESSIONS = new ConcurrentHashMap<>();

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final Gson gson = new Gson();

    @Resource
    private MessageService messageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            try {
                session.close();
            } catch (Exception ignored) {
            }
            return;
        }
        USER_SESSIONS.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("websocket connected, userId={}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        Long senderId = getUserId(session);
        if (senderId == null) {
            return;
        }
        ChatMessage chatMessage;
        try {
            chatMessage = gson.fromJson(textMessage.getPayload(), ChatMessage.class);
        } catch (Exception e) {
            return;
        }
        if (chatMessage == null || StringUtils.isBlank(chatMessage.getType())) {
            return;
        }
        // 心跳
        if ("ping".equals(chatMessage.getType())) {
            Map<String, Object> pong = new HashMap<>();
            pong.put("type", "pong");
            send(session, pong);
            return;
        }
        if (!"chat".equals(chatMessage.getType())) {
            return;
        }
        Long toUserId = chatMessage.getToUserId();
        String content = chatMessage.getContent();
        if (toUserId == null || toUserId <= 0 || StringUtils.isBlank(content) || content.length() > 1024) {
            sendError(session, "消息内容不合法");
            return;
        }
        if (toUserId.equals(senderId)) {
            sendError(session, "不能给自己发消息");
            return;
        }
        // 落库
        Message saved = messageService.saveMessage(senderId, toUserId, content);
        Date createTime = saved.getCreateTime() == null ? new Date() : saved.getCreateTime();
        String timeText = new SimpleDateFormat(DATE_FORMAT).format(createTime);

        // 回执给发送方
        Map<String, Object> ack = new HashMap<>();
        ack.put("type", "ack");
        ack.put("id", saved.getId());
        ack.put("createTime", timeText);
        send(session, ack);

        // 推送给接收方（在线则实时收到）
        Map<String, Object> push = new HashMap<>();
        push.put("type", "chat");
        push.put("id", saved.getId());
        push.put("senderId", senderId);
        push.put("receiverId", toUserId);
        push.put("content", content);
        push.put("createTime", timeText);
        pushToUser(toUserId, push);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                USER_SESSIONS.remove(userId);
            }
        }
    }

    /**
     * 当前在线用户 id 集合
     */
    public Set<Long> onlineUserIds() {
        return USER_SESSIONS.keySet();
    }

    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void pushToUser(Long userId, Object payload) {
        Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String json = gson.toJson(payload);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                sendText(session, json);
            }
        }
    }

    private void send(WebSocketSession session, Map<String, Object> payload) {
        sendText(session, gson.toJson(payload));
    }

    private void sendError(WebSocketSession session, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "error");
        payload.put("message", message);
        send(session, payload);
    }

    private void sendText(WebSocketSession session, String json) {
        try {
            // WebSocketSession 非线程安全，发送时加锁避免并发写异常
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            log.error("websocket send error", e);
        }
    }
}
