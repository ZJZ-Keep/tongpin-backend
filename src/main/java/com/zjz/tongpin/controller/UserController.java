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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
     * 头像本地存储目录
     */
    @Value("${file.avatar.upload-dir:./upload/avatar}")
    private String avatarUploadDir;

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
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
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
    public BaseResponse<Integer> userLogout(HttpServletRequest request, HttpServletResponse response) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request, response);
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
     * 根据 id 获取用户信息（用于聊天等场景）
     */
    @ApiOperation("根据id获取用户")
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getUserLogin(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return ResultUtils.success(userService.getSafetyUser(user));
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
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(defaultValue = "1") long pageNum,
            HttpServletRequest request) {
        User loginUser = userService.getUserLogin(request);
        // 未登录直接返回业务错误
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 排除自己，且按创建时间倒序，保证新注册用户也能被别人看到
        queryWrapper.ne("id", loginUser.getId());
        queryWrapper.orderByDesc("createTime");
        Page<User> userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        if (userPage != null && userPage.getRecords() != null) {
            userPage.setRecords(userPage.getRecords().stream()
                    .map(userService::getSafetyUser)
                    .collect(Collectors.toList()));
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
     * 搜索用户（支持按昵称 + 标签组合搜索）
     */
    @ApiOperation("搜索用户（昵称 + 标签）")
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList,
                                                     @RequestParam(required = false) String username,
                                                     HttpServletRequest request){
        User loginUser = userService.getUserLogin(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean hasTags = !CollectionUtils.isEmpty(tagNameList);
        boolean hasName = StringUtils.isNotBlank(username);
        // 没有任何搜索条件时直接返回空列表
        if (!hasTags && !hasName) {
            return ResultUtils.success(new ArrayList<>());
        }
        List<User> users;
        if (hasTags) {
            // 标签精确匹配（命中任意一个标签即可）
            users = userService.searchUserByTagName(tagNameList);
        } else {
            // 只按昵称搜索
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.like("username", username.trim());
            users = userService.list(wrapper);
        }
        String keyword = hasName ? username.trim().toLowerCase() : null;
        List<User> result = users.stream()
                // 排除自己
                .filter(user -> !user.getId().equals(loginUser.getId()))
                // 同时按昵称过滤（昵称不区分大小写）
                .filter(user -> {
                    if (keyword == null) {
                        return true;
                    }
                    String name = user.getUsername();
                    return name != null && name.toLowerCase().contains(keyword);
                })
                .map(userService::getSafetyUser)
                .collect(Collectors.toList());
        return ResultUtils.success(result);
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
        if (userLogin == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
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
                16, 30, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),new ThreadPoolExecutor.CallerRunsPolicy()
        );
        CurrentTableListener tableListener = new CurrentTableListener(userService, executor);
        EasyExcel.read(file.getInputStream(), UserTableInfo.class, tableListener).sheet().doRead();
        executor.shutdown();
        return ResultUtils.success("批量插入用户成功");
    }

    /**
     * 上传用户头像（保存到本地目录，返回可访问的图片地址）
     */
    @ApiOperation("上传用户头像")
    @PostMapping("/avatar/upload")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择图片");
        }
        User userLogin = userService.getUserLogin(request);
        if (userLogin == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 校验文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (StringUtils.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        if (!(".jpg".equals(suffix) || ".jpeg".equals(suffix) || ".png".equals(suffix)
                || ".gif".equals(suffix) || ".webp".equals(suffix))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持 jpg/png/gif/webp 格式图片");
        }
        // 限制大小 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过 5MB");
        }
        try {
            File dir = Paths.get(avatarUploadDir).toAbsolutePath().toFile();
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建头像目录失败");
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            // 返回带 context-path 的可访问地址
            return ResultUtils.success("/api/avatar/" + fileName);
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传头像失败");
        }
    }
}

