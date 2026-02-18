package com.zjz.tongpin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.tongpin.common.ResultUtils;
import com.zjz.tongpin.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
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
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("zjzString","zjz");
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

        redisTemplate.delete("zjzCode");
    }


}
