package com.itick.client.controller;

import com.itick.client.model.KlineData;
import com.itick.client.service.KlineService;
import com.itick.client.service.WebSocketClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 产品数据控制器
 * 提供HTTP API接口来获取K线数据和订阅实时数据
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StockDataController {

    private final KlineService klineService;
    private final WebSocketClientService webSocketClientService;

    /**
     * 获取特定股票/外汇/指数的K线数据
     * 
     * @param region 市场代码
     * @param code 产品代码
     * @param kType 周期类型
     * @param endTime 结束时间（可选）
     * @param limit 返回记录数量（可选）
     * @return K线数据列表
     */
    @GetMapping("/kline")
    public Mono<ResponseEntity<List<KlineData>>> getKlineData(
            @RequestParam String region,
            @RequestParam String code,
            @RequestParam int kType,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) Integer limit) {
        
        return klineService.getKlineData(region, code, kType, endTime, limit)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("获取K线数据时出错", e));
    }

    /**
     * 订阅实时产品数据
     * 
     * @param symbols 产品代码，多个代码用逗号分隔
     * @param types 数据类型，可选值：quote(报价)、tick(成交)、depth(盘口)
     * @return 订阅结果
     */
    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(
            @RequestParam String symbols,
            @RequestParam String types) {
        
        try {
            webSocketClientService.subscribe(symbols, types);
            return ResponseEntity.ok("已发送订阅请求，产品代码: " + symbols + "，数据类型: " + types);
        } catch (Exception e) {
            log.error("订阅产品数据时出错", e);
            return ResponseEntity.internalServerError().body("订阅失败: " + e.getMessage());
        }
    }
}
