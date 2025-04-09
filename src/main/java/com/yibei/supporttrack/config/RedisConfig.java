package com.yibei.supporttrack.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    private ObjectMapper createSafeObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 可见性配置（允许字段直接序列化）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // 安全配置
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 类型安全验证（扩展允许类型范围）
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util.")
                .allowIfSubType("com.yibei.")
                .allowIfSubType(Object.class) // 允许基础类型
                .build();

        // 类型信息配置（确保写入类型信息）
        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.EVERYTHING,  // 修改为 EVERYTHING
                JsonTypeInfo.As.PROPERTY
        );
        
        return objectMapper;
    }

    @Bean
    public RedisSerializer<Object> redisSerializer() {
        return new Jackson2JsonRedisSerializer<>(createSafeObjectMapper(), Object.class);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // 统一字符编码配置
        RedisSerializer<String> stringSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);
        
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(redisSerializer());
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(redisSerializer());
        
        // 启用事务支持
        template.setEnableTransactionSupport(true);
        return template;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 默认配置（1天过期）
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer()))
                .entryTtl(Duration.ofDays(1))
                .disableCachingNullValues();

        // 个性化缓存配置示例
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("userCache", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
