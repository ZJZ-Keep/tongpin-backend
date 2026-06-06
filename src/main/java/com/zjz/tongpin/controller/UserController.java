package com.zjz.tongpin.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.tongpin.common.BaseResponse;
import com.zjz.tongpin.common.ErrorCode;
import com.zjz.tongpin.common.ResultUtils;
import com.zjz.tongpin.exception.BusinessException;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.model.request.UserLoginRequest;
import com.zjz.tongpin.model.request.UserRegisterRequest;
import com.zjz.tongpin.once.CurrentTableListener;
import com.zjz.tongpin.once.UserTableInfo;
import com.zjz.tongpin.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zjz.tongpin.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 *   *   */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户接口")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @ApiOperation("用户注销")
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @ApiOperation("获取当前用户")
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        // 判断是否登录
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        // 判断用户是否合法
        if (user==null){
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (user.getUserStatus()!=0){
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        // 脱敏
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 搜索用户
     *
     * @param request
     * @return
     */
    @GetMapping("/search")
    @ApiOperation("搜索用户")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 推荐用户
     *
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    @ApiOperation("推荐用户")
    public BaseResponse<Page<User>> recommendUsers(
            @RequestParam long pageSize,
            @RequestParam long pageNum,
            HttpServletRequest request) {
        User loginUser = userService.getUserLogin(request);
        // 1. 缓存键加入分页参数，确保不同页数据缓存不冲突
        String redisKey = String.format("zjz:user:recommend:%s:%s:%s",
                loginUser.getId(), pageNum,pageSize);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 2. 尝试从缓存读取
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            // 缓存命中：对缓存中的数据进行脱敏
            return ResultUtils.success(userPage);
        }
        // 3. 缓存未命中，查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 4. 对查询出的原始数据进行脱敏
        if (userPage != null && userPage.getRecords() != null) {
            userPage.setRecords(userPage.getRecords().stream()
                    .map(userService::getSafetyUser)
                    .collect(Collectors.toList()));
        }
        // 5. 将脱敏后的分页结果写入缓存（建议添加过期时间）
        try {
            valueOperations.set(redisKey, userPage, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 删除用户
     *
     * @param id
     * @param request
     * @return
     */
    @ApiOperation("删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }



    /**
     * 通过标签搜索用户
     * @param tagNameList
     * @return
     */
    @ApiOperation("通过标签搜索用户")
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> users = userService.searchUserByTagName(tagNameList);
        return ResultUtils.success(users);
    }

    /**
     * 修改用户
     */
    @ApiOperation("修改用户")
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        log.info("修改用户{}:",user);
        if (user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
        if (userLogin==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int i = userService.updateUser(user,userLogin);
        return ResultUtils.success(i);
    }

    /**
     * 匹配用户
     */
    @ApiOperation("匹配用户")
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUser(long num,HttpServletRequest request){
        if (num<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
        List<User> userList=userService.matchUser(num,userLogin);
        return ResultUtils.success(userList);
    }

    /**
     * 批量插入用户
     * @param file
     */
    @PostMapping("/batchInsert")
    public BaseResponse<String> batchInsertUsers(MultipartFile  file) throws IOException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                8, 16, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),new ThreadPoolExecutor.CallerRunsPolicy()
        );
        CurrentTableListener tableListener = new CurrentTableListener(userService, executor);
        EasyExcel.read(file.getInputStream(), UserTableInfo.class, tableListener).sheet().doRead();
        executor.shutdown();
        return ResultUtils.success("批量插入用户成功");
    }
}

