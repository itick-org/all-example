package com.itick.client.model.websocket;

import lombok.Data;
import java.util.List;

/**
 * 盘口数据模型
 * 用于存储产品盘口信息（买卖档位）
 */
@Data
public class DepthData {
    private String s;           // 产品代码
    private List<OrderItem> a;  // 卖单列表
    private List<OrderItem> b;  // 买单列表
    private String type;        // 数据类型 (depth)
    
    /**
     * 订单项模型
     * 表示盘口中的一个买卖档位
     */
    @Data
    public static class OrderItem {
        private int po;     // 档位
        private double p;   // 价格
        private double v;   // 数量
        private double o;   // 订单数
    }
}
