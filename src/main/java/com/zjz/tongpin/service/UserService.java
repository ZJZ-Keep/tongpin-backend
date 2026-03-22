package com.zjz.tongpin.service;

import com.zjz.tongpin.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.zjz.tongpin.contant.UserConstant.ADMIN_ROLE;
import static com.zjz.tongpin.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务
 *
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    // [](https://github.com/ZJZ-Keep/tongpin-backend)

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 通过标签搜索用户
     * @param tags
     * @return
     */
    List<User> searchUserByTagName(List<String> tags);

    /**
     * 修改用户
     * @param user
     * @param userLogin
     * @return
     */
    int updateUser(User user,User userLogin);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
     boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param userLogin
     * @return
     */
    boolean isAdmin(User userLogin);

    /**
     * 得到当前用户
     */
    User getUserLogin(HttpServletRequest request);

    /**
     * 匹配用户
     */
    List<User> matchUser(long num, User userLogin);
}
