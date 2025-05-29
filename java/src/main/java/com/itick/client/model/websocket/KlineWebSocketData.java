package com.itick.client.model.websocket;

import com.itick.client.model.KlineData;
import lombok.Data;

/**
 * WebSocket K线数据模型
 * 用于存储通过WebSocket接收的K线数据
 */
@Data
public class KlineWebSocketData {
    private String s;     // 产品代码
    private int t;        // K线周期
    private KlineData k;  // K线数据
}
