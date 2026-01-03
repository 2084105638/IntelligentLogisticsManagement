package com.sylphy.controller;

import com.sylphy.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查 Controller
 *
 * @author apple
 * @since 2026/1/3
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("application", "Intelligent Logistics Management System");
        data.put("version", "1.0.0");
        return Result.success(data);
    }
}
