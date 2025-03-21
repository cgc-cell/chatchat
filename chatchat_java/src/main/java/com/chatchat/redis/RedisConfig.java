package com.chatchat.redis;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig <V>{


    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);
    @Value("${spring.redis.port:}")
    private Integer redisPort;

    @Value("${spring.redis.host:}")
    private String RedisHost;

    @Bean(name="redissonClient")
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + RedisHost + ":" + redisPort);
            RedissonClient redisson = Redisson.create(config);
            return redisson;
        } catch (Exception e) {
            log.error("redis配置错误，请检测redis配置");
        }
        return null;
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, V> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, V> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setValueSerializer(RedisSerializer.json());
        template.setHashValueSerializer(RedisSerializer.json());
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.afterPropertiesSet();
        return template;
    }
}
