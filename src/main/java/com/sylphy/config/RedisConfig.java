package com.sylphy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 配置
 *
 * @author apple
 * @since 2026/1/3
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key 采用 String 的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // hash 的 key 也采用 String 的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // value 序列化方式采用 JSON
        template.setValueSerializer(RedisSerializer.json());
        // hash 的 value 序列化方式采用 JSON
        template.setHashValueSerializer(RedisSerializer.json());

        return template;
    }
}
