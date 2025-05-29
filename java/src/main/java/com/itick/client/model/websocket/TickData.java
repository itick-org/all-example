package com.itick.client.model.websocket;

import lombok.Data;

/**
 * 成交数据模型
 * 用于存储产品成交信息
 */
@Data
public class TickData {
    private String s;     // 产品代码
    private double ld;    // 最新价格
    private long v;       // 成交量
    private long t;       // 时间戳
    private String type;  // 数据类型 (tick)
}
