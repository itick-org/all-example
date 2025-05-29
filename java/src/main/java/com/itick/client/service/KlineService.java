package com.itick.client.service;

import com.itick.client.model.ApiResponse;
import com.itick.client.model.KlineData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * K线数据服务
 * 负责通过HTTP API获取K线数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KlineService {

    private final WebClient webClient;
    
    /**
     * 获取特定股票/外汇/指数的K线数据
     * 
     * @param region 市场代码
     * @param code 产品代码
     * @param kType 周期类型: 1=1分钟, 2=5分钟, 3=10分钟, 4=30分钟, 5=1小时, 6=2小时, 7=4小时, 8=1天, 9=1周, 10=1月
     * @param endTime 结束时间（可选）
     * @param limit 返回记录数量（可选）
     * @return K线数据列表
     */
    public Mono<List<KlineData>> getKlineData(String region, String code, int kType, Long endTime, Integer limit) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/crypto/kline")
                            .queryParam("region", region)
                            .queryParam("code", code)
                            .queryParam("kType", kType);
                    
                    if (endTime != null) {
                        uriBuilder.queryParam("et", endTime);
                    }
                    
                    if (limit != null) {
                        uriBuilder.queryParam("limit", limit);
                    }
                    
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<KlineData>>>() {})
                .map(response -> {
                    if (response.getCode() != 0) {
                        log.error("获取K线数据时出错: {}", response.getMsg());
                        throw new RuntimeException("获取K线数据失败: " + response.getMsg());
                    }
                    return response.getData();
                })
                .doOnError(error -> log.error("获取K线数据时出错", error));
    }
}
