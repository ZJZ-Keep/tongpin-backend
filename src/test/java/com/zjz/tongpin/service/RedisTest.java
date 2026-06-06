package com.zjz.tongpin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.tongpin.common.ResultUtils;
import com.zjz.tongpin.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;
    @Test
    void RedisTest1(){
        /*HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        String userKey = "zjz:user:";
        hashOperations.put(userKey,"id",1);
        hashOperations.put(userKey,"username","zjz");
        hashOperations.get(userKey,"username");
        Map<String, Object> entries = hashOperations.entries(userKey);
        System.out.println(entries);
        Set<String> keys = hashOperations.keys(userKey);
        System.out.println(keys);
        // 设置过期时间
        redisTemplate.expire(userKey,30,TimeUnit.SECONDS);*/

        /*ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("zjzhello","hello");
        valueOperations.set("zjzzzz","zzzz", 30, TimeUnit.SECONDS);
        redisTemplate.delete("zjzhello");
        Object zjzzzz = valueOperations.get("zjzzzz");
        System.out.println(zjzzzz);*/
/*        valueOperations.set("zjzAge",20);
        valueOperations.set("zjzCode",2.0);
        User user = new User();
        user.setId(11L);
        user.setUsername("woqu");
        valueOperations.set("zjzUser",user);

        Object zjz = valueOperations.get("zjzString");
        Assertions.assertTrue("zjz".equals((String) zjz));
        zjz = valueOperations.get("zjzAge");
        Assertions.assertTrue(20==((Integer) zjz));
        zjz = valueOperations.get("zjzCode");
        Assertions.assertTrue(2.0==((Double) zjz));
        System.out.println(valueOperations.get("zjzUser"));*/

        //redisTemplate.delete("zjzCode");
    }


}
