package com.zjz.tongpin.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.tongpin.common.ResultUtils;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
//
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    private List<Long> usersId= Collections.singletonList(1L);
    @Scheduled(cron = "0 0 0 * * *")
    void doCacheRecommendUser() throws InterruptedException {
        RLock lock = redissonClient.getLock("zjz:precache:recommend:lock");
        Thread thread = Thread.currentThread();
        System.out.println("thread = " + thread.getId());
        try {
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                for (Long userId : usersId) {
                    // 1. 缓存键加入分页参数，确保不同页数据缓存不冲突
                    String redisKey = String.format("yupao:user:recommend:%s:%s:%s",
                            userId, 1,20);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 2. 缓存未命中，查询数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    // 3. 对查询出的原始数据进行脱敏
                    if (userPage != null && userPage.getRecords() != null) {
                        userPage.setRecords(userPage.getRecords().stream()
                                .map(userService::getSafetyUser)
                                .collect(Collectors.toList()));
                    }
                    // 4. 将脱敏后的分页结果写入缓存（建议添加过期时间）
                    try {
                        valueOperations.set(redisKey, userPage, 30, TimeUnit.MINUTES);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if (lock.isHeldByCurrentThread()){
                System.out.println("thread = " + thread.getId());
                Thread.sleep(10000);
                lock.unlock();
            }
        }


    }
}
