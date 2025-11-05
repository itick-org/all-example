package com.itick.client.example;

import com.itick.client.model.KlineData;
import com.itick.client.model.websocket.QuoteData;
import com.itick.client.service.KlineService;
import com.itick.client.service.WebSocketClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 产品数据示例应用
 * 演示如何使用客户端订阅WebSocket数据和获取K线数据
 * 
 * 这是一个命令行示例，可以运行它来测试功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockDataExample implements CommandLineRunner {

    private final WebSocketClientService webSocketClientService;
    private final KlineService klineService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void run(String... args) {
        log.info("启动产品数据示例...");
        log.info("按 1 测试WebSocket订阅");
        log.info("按 2 测试K线数据获取");
        log.info("按 q 退出");
        
        Scanner scanner = new Scanner(System.in);
        String input;
        
        while (!(input = scanner.nextLine()).equalsIgnoreCase("q")) {
            switch (input) {
                case "1":
                    testWebSocketSubscription();
                    break;
                case "2":
                    testKlineDataFetching();
                    break;
                default:
                    log.info("无效选项。请按 1、2 或 q");
            }
        }
        
        log.info("退出产品数据示例");
    }
    
    /**
     * 测试WebSocket订阅
     */
    private void testWebSocketSubscription() {
        log.info("测试WebSocket订阅...");
        
        // 创建一个锁存器来等待一些数据
        CountDownLatch latch = new CountDownLatch(3);
        
        // 订阅报价数据
        webSocketClientService.subscribeToQuotes(quoteData -> {
            displayQuoteData(quoteData);
            latch.countDown();
        });
        
        // 订阅ETHUSDT的报价数据
        log.info("正在订阅ETHUSDT的报价数据...");
        webSocketClientService.subscribe("ETHUSDT$ba", "quote");
        
        try {
            // 等待一些数据或30秒后超时
            boolean received = latch.await(30, TimeUnit.SECONDS);
            
            if (received) {
                log.info("成功接收到报价数据！");
            } else {
                log.warn("等待报价数据超时。请确保您的API令牌有效且市场开放。");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("等待报价数据时被中断", e);
        }
    }
    
    /**
     * 测试K线数据获取
     */
    private void testKlineDataFetching() {
        log.info("测试K线数据获取...");
        log.info("获取ETHUSDT的日K线数据...");
        
        // 获取产品的日K线数据（最近10天）
        klineService.getKlineData("BA", "ETHUSDT", 8, null, 10)
                .subscribe(
                        this::displayKlineData,
                        error -> log.error("获取K线数据时出错", error),
                        () -> log.info("K线数据获取完成")
                );
    }
    
    /**
     * 显示报价数据
     * 
     * @param quoteData 报价数据
     */
    private void displayQuoteData(QuoteData quoteData) {
        LocalDateTime time = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(quoteData.getT()), 
                ZoneId.systemDefault()
        );
        
        log.info("{} 在 {} 的报价数据:", quoteData.getS(), DATE_FORMATTER.format(time));
        log.info("  最新价: {}", quoteData.getLd());
        log.info("  开盘价: {}", quoteData.getO());
        log.info("  最高价: {}", quoteData.getH());
        log.info("  最低价: {}", quoteData.getL());
        log.info("  成交量: {}", quoteData.getV());
    }
    
    /**
     * 显示K线数据
     * 
     * @param klineDataList K线数据列表
     */
    private void displayKlineData(List<KlineData> klineDataList) {
        log.info("收到 {} 条K线数据", klineDataList.size());
        
        for (KlineData kline : klineDataList) {
            LocalDateTime time = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(kline.getT()), 
                    ZoneId.systemDefault()
            );
            
            log.info("{} 的K线数据:", DATE_FORMATTER.format(time));
            log.info("  开盘价: {}", kline.getO());
            log.info("  最高价: {}", kline.getH());
            log.info("  最低价: {}", kline.getL());
            log.info("  收盘价: {}", kline.getC());
            log.info("  成交量: {}", kline.getV());
        }
    }
}
