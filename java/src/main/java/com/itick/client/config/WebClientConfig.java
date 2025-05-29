package com.itick.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient配置类
 * 配置用于HTTP请求的WebClient
 */
@Configuration
public class WebClientConfig {

    @Value("${itick.api.base-url}")
    private String baseUrl;

    @Value("${itick.api.token}")
    private String apiToken;

    /**
     * 创建WebClient Bean
     * 用于发送HTTP请求到iTick.org API
     * 
     * @return 配置好的WebClient实例
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("token", apiToken)
                .build();
    }
}
