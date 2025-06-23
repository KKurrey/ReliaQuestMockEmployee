package com.reliaquest.api.config;

import com.reliaquest.api.constants.ExceptionConstants;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    private final RedisConnectionFactory connectionFactory;

    public RedisConfig(RedisConnectionFactory connectionFactory) {

        if (connectionFactory == null) {
            throw new IllegalArgumentException(ExceptionConstants.EXC_REDIS_CONNECTION_FACTORY_NULL);
        }
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void init() {
        log.info(
                "RedisConfig initialized with connection factory: {}",
                connectionFactory.getClass().getSimpleName());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        log.info("RedisTemplate configured with StringRedisSerializer and GenericJackson2JsonRedisSerializer");

        return template;
    }
}
