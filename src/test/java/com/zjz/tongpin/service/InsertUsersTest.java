package com.zjz.tongpin.service;

import com.zjz.tongpin.model.domain.User;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 导入用户测试
 *
 * @author <a href="https://github.com/ZJZ-Keep"></a>
 *
 */
@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
     private ExecutorService executorService2 =new ThreadPoolExecutor(8,8,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

     /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("https://github.com/ZJZ-Keep/tongpin-backend");
            user.setUserAccount("fakeyupi");
            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("11111111");
            userList.add(user);
        }
        // 20 秒 100 万条
        userService.saveBatch(userList, 100000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int once = 1000;
        int j=0;
        List<CompletableFuture> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("https://github.com/ZJZ-Keep/tongpin-backend");
                user.setUserAccount("fakeyupi");
                user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123");
                user.setEmail("123@qq.com");
                user.setTags("[]");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("11111111");
                userList.add(user);
                if (j%once==0){
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName:" + Thread.currentThread());
                userService.saveBatch(userList, once);
            },executorService2);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }

}

//68

//1 62500*16  28778

//2 62500*16  24503

//2 1000*1000 19112
