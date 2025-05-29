package com.itick.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itick.client.model.websocket.*;
import com.itick.client.model.websocket.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * WebSocket客户端服务
 * 负责处理与iTick.org WebSocket API的连接、认证、订阅和数据接收
 */
@Slf4j
@Service
public class WebSocketClientService extends TextWebSocketHandler {

    @Value("${itick.api.websocket-url}")
    private String webSocketUrl;

    @Value("${itick.api.token}")
    private String apiToken;

    private WebSocketSession session;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> heartbeatTask;
    
    // 不同类型数据的订阅者列表
    private final List<Consumer<QuoteData>> quoteSubscribers = new ArrayList<>();
    private final List<Consumer<TickData>> tickSubscribers = new ArrayList<>();
    private final List<Consumer<DepthData>> depthSubscribers = new ArrayList<>();
    private final List<Consumer<KlineWebSocketData>> klineSubscribers = new ArrayList<>();

    /**
     * 初始化WebSocket连接
     * 在Spring容器启动后自动调用
     */
    @PostConstruct
    public void init() {
        connect();
    }

    /**
     * 清理资源
     * 在Spring容器关闭前自动调用
     */
    @PreDestroy
    public void cleanup() {
        stopHeartbeat();
        disconnect();
    }

    /**
     * 建立WebSocket连接
     */
    public void connect() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            session = client.execute(this, null, URI.create(webSocketUrl)).get();
            log.info("WebSocket连接已建立");
            Thread.sleep(1000);
            authenticate();
        } catch (Exception e) {
            log.error("连接WebSocket服务器失败", e);
        }
    }

    /**
     * 断开WebSocket连接
     */
    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
                log.info("WebSocket连接已关闭");
            } catch (IOException e) {
                log.error("关闭WebSocket连接时出错", e);
            }
        }
    }

    /**
     * 连接建立后的回调方法
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws InterruptedException {

    }

    /**
     * 进行API认证
     */
    private void authenticate() {
        WebSocketMessage authMessage = WebSocketMessage.builder()
                .ac("auth")
                .params(apiToken)
                .build();
        
        sendMessage(authMessage);
    }

    /**
     * 订阅产品数据
     * 
     * @param symbols 产品代码，多个代码用逗号分隔
     * @param types 数据类型，可选值：quote(报价)、tick(成交)、depth(盘口)
     */
    public void subscribe(String symbols, String types) {
        if (session == null || !session.isOpen()) {
            log.error("无法订阅，WebSocket连接未建立");
            return;
        }

        WebSocketMessage subscribeMessage = WebSocketMessage.builder()
                .ac("subscribe")
                .params(symbols)
                .types(types)
                .build();
        
        sendMessage(subscribeMessage);
    }

    /**
     * 启动心跳机制
     */
    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatTask = heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 15, 15, TimeUnit.SECONDS);
    }

    /**
     * 停止心跳机制
     */
    private void stopHeartbeat() {
        if (heartbeatTask != null && !heartbeatTask.isCancelled()) {
            heartbeatTask.cancel(true);
        }
    }

    /**
     * 发送心跳消息
     */
    private void sendHeartbeat() {
        if (session != null && session.isOpen()) {
            long timestamp = System.currentTimeMillis();
            WebSocketMessage pingMessage = WebSocketMessage.builder()
                    .ac("ping")
                    .params(String.valueOf(timestamp))
                    .build();
            
            sendMessage(pingMessage);
        }
    }

    /**
     * 发送WebSocket消息
     */
    private void sendMessage(WebSocketMessage message) {
        try {
            if (session != null && session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("已发送消息: {}", jsonMessage);
            } else {
                log.error("无法发送消息，WebSocket连接未建立");
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息时出错", e);
        }
    }

    /**
     * 处理接收到的WebSocket文本消息
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.info("接收到消息: {}", payload);
            
            JsonNode rootNode = objectMapper.readTree(payload);
            int code = rootNode.path("code").asInt();
            
            if (rootNode.has("resAc")) {
                String resAc = rootNode.path("resAc").asText();
                
                if ("auth".equals(resAc)) {
                    handleAuthResponse(code, rootNode);
                } else if ("subscribe".equals(resAc)) {
                    handleSubscribeResponse(code, rootNode);
                } else if ("pong".equals(resAc)) {
                    log.debug("收到pong响应");
                }
            } else if (rootNode.has("data")) {
                JsonNode dataNode = rootNode.path("data");
                
                if (dataNode.has("type")) {
                    String type = dataNode.path("type").asText();
                    
                    switch (type) {
                        case "quote":
                            QuoteData quoteData = objectMapper.treeToValue(dataNode, QuoteData.class);
                            notifyQuoteSubscribers(quoteData);
                            break;
                        case "tick":
                            TickData tickData = objectMapper.treeToValue(dataNode, TickData.class);
                            notifyTickSubscribers(tickData);
                            break;
                        case "depth":
                            DepthData depthData = objectMapper.treeToValue(dataNode, DepthData.class);
                            notifyDepthSubscribers(depthData);
                            break;
                        default:
                            if (dataNode.has("k")) {
                                KlineWebSocketData klineData = objectMapper.treeToValue(dataNode, KlineWebSocketData.class);
                                notifyKlineSubscribers(klineData);
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息时出错", e);
        }
    }

    /**
     * 处理认证响应
     */
    private void handleAuthResponse(int code, JsonNode rootNode) {
        if (code == 1) {
            log.info("认证成功");
            startHeartbeat();
        } else {
            log.error("认证失败: {}", rootNode.path("msg").asText());
            disconnect();
        }
    }

    /**
     * 处理订阅响应
     */
    private void handleSubscribeResponse(int code, JsonNode rootNode) {
        if (code == 1) {
            log.info("订阅成功");
        } else {
            log.error("订阅失败: {}", rootNode.path("msg").asText());
        }
    }

    /**
     * 处理WebSocket传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误", exception);
        stopHeartbeat();
        scheduleReconnect();
    }

    /**
     * 连接关闭后的回调方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket连接已关闭: {}", status);
        stopHeartbeat();
        scheduleReconnect();
    }

    /**
     * 安排重新连接
     */
    private void scheduleReconnect() {
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(this::connect);
    }

    // 订阅不同类型数据的方法

    /**
     * 订阅报价数据
     * @param subscriber 报价数据订阅者
     */
    public void subscribeToQuotes(Consumer<QuoteData> subscriber) {
        quoteSubscribers.add(subscriber);
    }

    /**
     * 订阅成交数据
     * @param subscriber 成交数据订阅者
     */
    public void subscribeToTicks(Consumer<TickData> subscriber) {
        tickSubscribers.add(subscriber);
    }

    /**
     * 订阅盘口数据
     * @param subscriber 盘口数据订阅者
     */
    public void subscribeToDepth(Consumer<DepthData> subscriber) {
        depthSubscribers.add(subscriber);
    }

    /**
     * 订阅K线数据
     * @param subscriber K线数据订阅者
     */
    public void subscribeToKlines(Consumer<KlineWebSocketData> subscriber) {
        klineSubscribers.add(subscriber);
    }

    // 通知不同类型数据的方法

    /**
     * 通知报价数据订阅者
     */
    private void notifyQuoteSubscribers(QuoteData data) {
        quoteSubscribers.forEach(subscriber -> subscriber.accept(data));
    }

    /**
     * 通知成交数据订阅者
     */
    private void notifyTickSubscribers(TickData data) {
        tickSubscribers.forEach(subscriber -> subscriber.accept(data));
    }

    /**
     * 通知盘口数据订阅者
     */
    private void notifyDepthSubscribers(DepthData data) {
        depthSubscribers.forEach(subscriber -> subscriber.accept(data));
    }

    /**
     * 通知K线数据订阅者
     */
    private void notifyKlineSubscribers(KlineWebSocketData data) {
        klineSubscribers.forEach(subscriber -> subscriber.accept(data));
    }
}
