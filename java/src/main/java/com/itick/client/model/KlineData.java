package com.itick.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * K线数据模型
 * 表示一个K线周期的价格和成交数据
 */
@Data
public class KlineData {
    private double tu; // 成交金额
    private double c;  // 该K线收盘价
    private long t;    // 时间戳
    private int v;     // 成交数量
    private double h;  // 该K线最高价
    private double l;  // 该K线最低价
    private double o;  // 该K线开盘价
}
