package com.itick.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Java客户端应用程序的主类
 * 用于启动Spring Boot应用程序
 */
@SpringBootApplication
@EnableScheduling
public class JavaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaClientApplication.class, args);
    }
}
