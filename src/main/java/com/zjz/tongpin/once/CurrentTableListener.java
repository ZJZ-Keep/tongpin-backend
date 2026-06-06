package com.zjz.tongpin.once;
import java.util.Date;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
@Slf4j
public class CurrentTableListener implements ReadListener<UserTableInfo> {

    private static final String SALT = "zjz";
    private static final int BATCH_COUNT = 1000;
    private final List<UserTableInfo> cachedDataList = new ArrayList<>();
    private final List<CompletableFuture<Void>>  futures = new ArrayList<>();

    private final UserService userService;
    private final ExecutorService executorService;
    public CurrentTableListener(UserService userService, ExecutorService executorService){
        this.userService = userService;
        this.executorService = executorService;
    }
    @Override
    public void invoke(UserTableInfo userTableInfo, AnalysisContext analysisContext) {
        cachedDataList.add(userTableInfo);
        if (cachedDataList.size() >= BATCH_COUNT) {
            process();
        }
    }

    public void process(){
        // 批量处理链表
        ArrayList<UserTableInfo> batchList = new ArrayList<>(cachedDataList);
        cachedDataList.clear();
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<String> userCodes = batchList.stream()
                        .map(UserTableInfo::getPlanetCode)
                        .collect(Collectors.toList());

                // 查询数据库中已存在的用户
                Set<String> existUserCodes = getExistUserCodes(userCodes);

                // 筛选出新用户
                List<UserTableInfo> newUserTableInfos = batchList.stream()
                        .filter(userTableInfo ->
                                !existUserCodes.contains(userTableInfo.getPlanetCode())
                        )
                        .collect(Collectors.toList());

                // 转换为User对象
                List<User> newUsers = conventToUser(newUserTableInfos);

                // 批量插入数据库
                userService.saveBatch(newUsers);
            }, executorService);
            futures.add(future);
        } catch (Exception e) {
            log.error("批量插入失败：{}",e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!cachedDataList.isEmpty()){
            process();
        }
        log.info("等待所有数据解析完成！");
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        log.info("所有数据解析完成,共{}个批次",futures.size());
    }

    private Set<String> getExistUserCodes(List<String> userCodes){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("planetCode", userCodes);
        List<User> userList = userService.list(queryWrapper);
        return userList.stream()
                .map(User::getPlanetCode)
                .collect(Collectors.toSet());
    }

    private List<User> conventToUser(List<UserTableInfo> userTableInfos){
        ArrayList<User> users = new ArrayList<>();
        for (UserTableInfo userTableInfo : userTableInfos){
            User user = new User();
            user.setId(0L);
            user.setGender(0);
            user.setUserStatus(0);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setIsDelete(0);
            user.setUserRole(0);
            user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "88888888").getBytes()));
            user.setUsername(userTableInfo.getUsername());
            user.setUserAccount(userTableInfo.getUsername());
            users.add(user);
        }
        return users;
    }
}
