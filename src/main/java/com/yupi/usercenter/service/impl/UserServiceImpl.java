package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.exception.WxException;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.yupi.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 34234
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-12-01 10:56:33
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final String SALT = "wxx";


    @Autowired
    private UserMapper userMapper;

    /**
     * 注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {

        //输入非空判断
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return -1;
        }
        //账号不小于四位
        if (userAccount.length() < 4) {
            return -1;
        }
        //密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            return -1;
        }
        //星球id
        if (planetCode.length() > 5) {
            return -1;
        }
        //账号不包含特殊字符
        // 1.5 校验账号不包含特殊字符
        /*  pP和pS匹配特殊符号
            \s+是空格一个或者多个,不管在那个位置都能匹配
            \pP 其中的小写 p 是 property 的意思，表示 Unicode 属性，用于 Unicode 正表达式的前缀。
            大写 P 表示 Unicode 字符集七个字符属性之一：标点字符。
            大写 P 表示 符号（比如数学符号、货币符号等）
        */
        String validPattern = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        //密码要和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        //账号不能重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            return -1;
        }
        //星球id不能重复
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPlanetCode, planetCode);
        count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            return -1;
        }
        //密码加密再放到数据库里

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean isSave = this.save(user);
        if (!isSave) {
            return -1;
        }
        return user.getId();
    }

    /**
     * 登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request, String captcha, String captchaCode) {
        String captchaCodeRedis = redisTemplate.opsForValue().get(captcha);
        // 验证码不存在（已过期），抛出异常
        if (captchaCodeRedis == null) {
            throw new RuntimeException("验证码已过期");
        }
        // 比较用户输入的验证码与Redis中的验证码，不匹配则抛出异常
        if (!captchaCode.toLowerCase().equals(captchaCodeRedis)) {
            throw new RuntimeException("验证码不正确");
        }
        //输入非空判断
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        //账号不小于四位
        if (userAccount.length() < 4) {
            return null;
        }
        //密码不小于8位
        if (userPassword.length() < 8) {
            return null;
        }
        //账号不包含特殊字符
        // 1.5 校验账号不包含特殊字符
        /*  pP和pS匹配特殊符号
            \s+是空格一个或者多个,不管在那个位置都能匹配
            \pP 其中的小写 p 是 property 的意思，表示 Unicode 属性，用于 Unicode 正表达式的前缀。
            大写 P 表示 Unicode 字符集七个字符属性之一：标点字符。
            大写 P 表示 符号（比如数学符号、货币符号等）
        */
        String validPattern = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        //校验密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        wrapper.eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(wrapper);
        if (user == null) {
            log.info("user long failed, userAccount cannot match userPassword");
            return null;
        }
        //用户脱敏
        User safetyUser = getSafetyUser(user);
        //删除用户登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        // 4. 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 脱敏
     *
     * @param user
     * @return
     */
    @Override
    public User getSafetyUser(User user) {
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setUsername(user.getUsername());
        newUser.setUserAccount(user.getUserAccount());
        newUser.setAvatarUrl(user.getAvatarUrl());
        newUser.setGender(user.getGender());
        newUser.setPhone(user.getPhone());
        newUser.setEmail(user.getEmail());
        newUser.setProfile(user.getProfile());
        newUser.setTags(user.getTags());
        newUser.setPlanetCode(user.getPlanetCode());
        newUser.setUserRole(user.getUserRole());
        newUser.setUserStatus(user.getUserStatus());
        newUser.setCreateTime(user.getCreateTime());
        return newUser;
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogOut(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new WxException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为管理员
     *
     * @param
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        if (user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return true;
    }

    /**
     * 获取当前登录用户
     * }
     *
     * @param request
     * @return
     */
    @Override
    public User getLogUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        if (currentUser == null) {
            throw new WxException(ErrorCode.NOT_LOGIN);
        }

        return currentUser;
    }

    /**
     * 更新用户
     *
     * @param user
     * @param logUser
     * @return
     */
    @Override
    public int updateUser(User user, User logUser) {
        //判空
        if (user == null || logUser == null) {
            throw new WxException(ErrorCode.PARAMS_ERROR);
        }
        //鉴权
        long oldId = user.getId();
        if (!isAdmin(logUser) && logUser.getId() != oldId) {
            throw new WxException(ErrorCode.PARAMS_ERROR, "不是管理员而且要改的信息还不是自己的");
        }
        User oldUser = userMapper.selectById(oldId);
        if (oldUser == null) {
            throw new WxException(ErrorCode.NULL_ERROR, "要更改的用户不存在");
        }
        //更改
        return userMapper.updateById(user);
    }

    @Override
    public User doLoginHuobanLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 验证码不存在（已过期），抛出异常
        // 比较用户输入的验证码与Redis中的验证码，不匹配则抛出异常
        //输入非空判断
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        //账号不小于四位
        if (userAccount.length() < 4) {
            return null;
        }
        //密码不小于8位
        if (userPassword.length() < 8) {
            return null;
        }
        //账号不包含特殊字符
        // 1.5 校验账号不包含特殊字符
        /*  pP和pS匹配特殊符号
            \s+是空格一个或者多个,不管在那个位置都能匹配
            \pP 其中的小写 p 是 property 的意思，表示 Unicode 属性，用于 Unicode 正表达式的前缀。
            大写 P 表示 Unicode 字符集七个字符属性之一：标点字符。
            大写 P 表示 符号（比如数学符号、货币符号等）
        */
        String validPattern = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        //校验密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        wrapper.eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(wrapper);
        if (user == null) {
            log.info("user long failed, userAccount cannot match userPassword");
            return null;
        }
        //用户脱敏
        User safetyUser = getSafetyUser(user);
        //删除用户登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        // 4. 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }
}




