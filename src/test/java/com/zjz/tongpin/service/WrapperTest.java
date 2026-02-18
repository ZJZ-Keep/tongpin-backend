package com.zjz.tongpin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zjz.tongpin.mapper.UserMapper;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class WrapperTest {


    @Resource
    private UserMapper userMapper;

    @Resource
    private UserServiceImpl userService;

    @Test
    void QueryWrapperTest(){
        QueryWrapper<User> wrapper = new QueryWrapper<User>()
                .select("id","userName","tags")
                .like("tags","java")
                .eq("UserRole","0");
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(System.out::println);
    }

    @Test
    void LambdaQueryWrapperTest(){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .select(User::getId,User::getUsername,User::getTags)
                .like(User::getTags,"java")
                .eq(User::getUserRole,"0");
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(System.out::println);
    }

    @Test
    void UpdateWrapperTest(){
        User user = new User();
        user.setEmail("789");
        UpdateWrapper<User> wrapper = new UpdateWrapper<User>()
                .setSql("phone=phone+45")
                .like("tags","java");
        userMapper.update(user,wrapper);
    }

    @Test
    void QueryCustomSqlWrapperTest(){
        QueryWrapper<User> wrapper = new QueryWrapper<User>()
                .like("tags","java");
        List<User> users = userMapper.selectListByCustomSql(wrapper);
        users.forEach(System.out::println);
    }

}
