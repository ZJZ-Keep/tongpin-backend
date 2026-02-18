package com.zjz.tongpin.config;

import io.lettuce.core.RedisClient;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    private String host;
    private String port;
    private String password;

    @Bean
    public RedissonClient redissonClient() throws IOException {
        Config config = new Config();
        String format = String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(format).setDatabase(3).setPassword(password);
        // Sync and Async API
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
