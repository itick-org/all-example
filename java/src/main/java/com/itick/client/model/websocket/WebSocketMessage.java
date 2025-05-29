package com.itick.client.model.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息模型
 * 用于WebSocket通信的消息格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {
    private String ac;      // 操作类型: auth(认证), subscribe(订阅), ping(心跳)
    private String params;  // 操作参数
    private String types;   // 订阅类型: depth(盘口), quote(报价), tick(成交)
    private String resAc;   // 响应操作
    private int code;       // 响应代码
    private String msg;     // 响应消息
    private Object data;    // 响应数据
}
