package com.better.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @param
 * @Date 2022/7/16
 */
@Configuration
public class RedisConfig {
    // springboot已经配置好了redisTemplate并加入组件了，但是配置的我们并不想要（应为key设成了Object）
    // springboot配置的在 RedisAutoConfiguration 类中
    // 这里我们重新配置并且注入一下（参考RedisAutoConfiguration）
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 1.设置redis连接工厂
        template.setConnectionFactory(factory);

        // 2.设置序列化方式（把java对象经过IO存储到redis中）
        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        // 设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());
        //让设置生效
        template.afterPropertiesSet();

        return template;
    }
}
