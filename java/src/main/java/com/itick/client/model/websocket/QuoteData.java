package com.itick.client.model.websocket;

import lombok.Data;

/**
 * 报价数据模型
 * 用于存储产品报价信息
 */
@Data
public class QuoteData {
    private String s;     // 产品代码
    private double ld;    // 最新价格
    private double o;     // 开盘价
    private double h;     // 最高价
    private double l;     // 最低价
    private long t;       // 时间戳
    private long v;       // 成交量
    private double tu;    // 成交额
    private long ts;      // 交易状态
    private String type;  // 数据类型 (quote)
}
