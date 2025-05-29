package com.itick.client.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itick.client.model.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * WebSocket工具类
 * 用于处理WebSocket消息的工具方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketUtils {

    private final ObjectMapper objectMapper;

    /**
     * 发送WebSocket消息到指定会话
     *
     * @param session WebSocket会话
     * @param message 要发送的WebSocket消息
     * @return 消息是否成功发送
     */
    public boolean sendMessage(WebSocketSession session, WebSocketMessage message) {
        if (session == null || !session.isOpen()) {
            log.error("无法发送消息，WebSocket会话为空或已关闭");
            return false;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.debug("已发送消息: {}", jsonMessage);
            return true;
        } catch (IOException e) {
            log.error("发送WebSocket消息时出错", e);
            return false;
        }
    }

    /**
     * 创建认证消息
     *
     * @param token API令牌
     * @return WebSocket认证消息
     */
    public WebSocketMessage createAuthMessage(String token) {
        return WebSocketMessage.builder()
                .ac("auth")
                .params(token)
                .build();
    }

    /**
     * 创建订阅消息
     *
     * @param symbols 要订阅的产品代码，多个代码用逗号分隔
     * @param types 要订阅的数据类型，多个类型用逗号分隔
     * @return WebSocket订阅消息
     */
    public WebSocketMessage createSubscribeMessage(String symbols, String types) {
        return WebSocketMessage.builder()
                .ac("subscribe")
                .params(symbols)
                .types(types)
                .build();
    }

    /**
     * 创建心跳消息
     *
     * @return WebSocket心跳消息
     */
    public WebSocketMessage createPingMessage() {
        return WebSocketMessage.builder()
                .ac("ping")
                .params(String.valueOf(System.currentTimeMillis()))
                .build();
    }

    /**
     * 从JSON解析WebSocket消息
     *
     * @param json 要解析的JSON字符串
     * @return 解析后的WebSocket消息
     * @throws JsonProcessingException 如果JSON无效
     */
    public WebSocketMessage parseMessage(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, WebSocketMessage.class);
    }
}
