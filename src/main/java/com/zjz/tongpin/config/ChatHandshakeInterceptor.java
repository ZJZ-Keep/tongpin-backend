package com.zjz.tongpin.config;

import com.zjz.tongpin.model.domain.User;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.zjz.tongpin.contant.UserConstant.USER_LOGIN_STATE;

/**
 * WebSocket 握手拦截器：从 HttpSession 中取出登录用户，写入 session 属性
 */
@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            Object userObj = servletRequest.getSession().getAttribute(USER_LOGIN_STATE);
            if (userObj instanceof User) {
                attributes.put("userId", ((User) userObj).getId());
                return true;
            }
        }
        // 未登录，拒绝握手
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // do nothing
    }
}
