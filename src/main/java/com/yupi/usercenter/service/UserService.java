package com.yupi.usercenter.service;

import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 34234
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-12-01 10:56:33
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword,String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    User doLogin(String userAccount, String userPassword, HttpServletRequest request, String captcha, String code);


    /**
     * 用户脱敏
     * @param
     * @return
     */
    User getSafetyUser(User user);

    /**
     * 退出登录
     * @param request
     * @return
     */
    boolean userLogOut(HttpServletRequest request);


    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 判断是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);
    /**
     * 判断是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLogUser(HttpServletRequest request);

    /**
     * 更新用户个人信息
     * @param user
     * @param logUser
     * @return
     */
    int updateUser(User user, User logUser);

    /**
     * 获取当前登录用户
     * 伙伴匹配
     * @param request
     * @return
     */
    User doLoginHuobanLogin(String userAccount, String userPassword, HttpServletRequest request);
}
