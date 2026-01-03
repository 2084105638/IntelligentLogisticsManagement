package com.sylphy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Intelligent Logistics Management System
 *
 * @author apple
 * @since 2026/1/3
 */
@SpringBootApplication
@MapperScan("com.sylphy.mapper")
public class LogisticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogisticsApplication.class, args);
    }
}
