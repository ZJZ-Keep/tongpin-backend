package com.zjz.tongpin.service;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.tongpin.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class PageTest {

    @Resource
    private UserService userService;
    @Test
    void page(){
        int pageNum=1,pageSize=5;
        Page<User> page = Page.of(pageNum, pageSize);
        page.addOrder(new OrderItem("id", false));
        Page<User> p = userService.page(page);
        List<User> records = p.getRecords();
        records.forEach(System.out::println);
        System.out.println(p.getPages());
        System.out.println(p.getTotal());
    }
}
