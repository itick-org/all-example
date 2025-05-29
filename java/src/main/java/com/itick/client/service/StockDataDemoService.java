package com.itick.client.service;

import com.itick.client.model.websocket.DepthData;
import com.itick.client.model.websocket.QuoteData;
import com.itick.client.model.websocket.TickData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 产品数据演示服务
 * 展示如何订阅和处理实时产品数据
 * 这是一个演示用途的服务，可以根据需要修改或删除
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataDemoService {

    private final WebSocketClientService webSocketClientService;

    /**
     * 应用程序启动后初始化
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // 订阅不同类型的数据事件
        subscribeToDataEvents();
        
        // 示例订阅 - 取消注释以激活
        // subscribeToStocks();
    }

    /**
     * 订阅数据事件
     */
    private void subscribeToDataEvents() {
        // 订阅报价数据
        webSocketClientService.subscribeToQuotes(this::processQuoteData);
        
        // 订阅成交数据
        webSocketClientService.subscribeToTicks(this::processTickData);
        
        // 订阅盘口数据
        webSocketClientService.subscribeToDepth(this::processDepthData);
    }

    /**
     * 订阅产品
     */
    private void subscribeToCrypto() {
        // 示例：订阅产品的报价和盘口数据
        webSocketClientService.subscribe("ETHUSDT", "tick,quote,depth");
    }

    /**
     * 处理报价数据
     * 
     * @param quoteData 报价数据
     */
    private void processQuoteData(QuoteData quoteData) {
        log.info("收到 {} 的报价数据: 最新价 = {}, 最高价 = {}, 最低价 = {}, 成交量 = {}",
                quoteData.getS(), quoteData.getLd(), quoteData.getH(), quoteData.getL(), quoteData.getV());
        
        // 在这里添加自定义处理逻辑
    }

    /**
     * 处理成交数据
     * 
     * @param tickData 成交数据
     */
    private void processTickData(TickData tickData) {
        log.info("收到 {} 的成交数据: 最新价 = {}, 成交量 = {}",
                tickData.getS(), tickData.getLd(), tickData.getV());
        
        // 在这里添加自定义处理逻辑
    }

    /**
     * 处理盘口数据
     * 
     * @param depthData 盘口数据
     */
    private void processDepthData(DepthData depthData) {
        log.info("收到 {} 的盘口数据: {} 个卖单, {} 个买单",
                depthData.getS(), 
                depthData.getA() != null ? depthData.getA().size() : 0,
                depthData.getB() != null ? depthData.getB().size() : 0);
        
        // 在这里添加自定义处理逻辑
    }
}
