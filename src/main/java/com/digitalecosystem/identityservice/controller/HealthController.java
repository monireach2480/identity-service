package com.digitalecosystem.identityservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "identity-service");
        health.put("timestamp", System.currentTimeMillis());

        // Check database
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("database_error", e.getMessage());
        }

        // Check Redis
        try {
            redisTemplate.opsForValue().set("health_check", "test");
            redisTemplate.delete("health_check");
            health.put("redis", "UP");
        } catch (Exception e) {
            health.put("redis", "DOWN");
            health.put("redis_error", e.getMessage());
        }

        return ResponseEntity.ok(health);
    }
}