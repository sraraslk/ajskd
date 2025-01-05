package com.yupi.usercenter.cotroller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.common.Result;
import com.yupi.usercenter.exception.WxException;
import com.yupi.usercenter.model.UserLoginHuoAbanRequest;
import com.yupi.usercenter.model.UserLoginRequest;
import com.yupi.usercenter.model.UserRegisterRequest;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static com.yupi.usercenter.contant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
//@CrossOrigin
//@CrossOrigin(origins = {"http://localhost:3000"},allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *用户中心
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
           //return null;
            throw new WxException(ErrorCode.PARAMS_ERROR);  
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,planetCode)) {
            return null;
        }
        long id = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        return Result.ok(id);
    }

    /**
     * 用户登录
     *用户中心
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new WxException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        String captcha = userLoginRequest.getCaptchaKey();
        String code = userLoginRequest.getCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword,captcha,code)) {
            throw new WxException(ErrorCode.NULL_ERROR);
        }
        User user = userService.doLogin(userAccount, userPassword, request,captcha,code);
      return Result.ok(user);
    }
    /**
     * 伙伴匹配用户登录
     *
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login/huoban")
    public BaseResponse<User> userLogin(@RequestBody UserLoginHuoAbanRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new WxException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new WxException(ErrorCode.NULL_ERROR);
        }
        User user = userService.doLoginHuobanLogin(userAccount, userPassword, request);
        return Result.ok(user);
    }

    /**
     * 本来用户中心的接口，伙伴匹配用了
     * @param request
     * @return
     */
    @PostMapping("/logOut")
    public BaseResponse<Boolean> userLogout( HttpServletRequest request) {
        if (request == null) {
           throw new WxException(ErrorCode.NO_AUTH);
        }
        boolean b = userService.userLogOut(request);
        return Result.ok(b);

    }

    /**
     *伙伴匹配和用户中心都有
     * 查看当前用户
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrent( HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        if (currentUser == null) {
          throw new WxException(ErrorCode.NOT_LOGIN);
        }
        Long id = currentUser.getId();
        User user = userService.getById(id);
        User safetyUser = userService.getSafetyUser(user);
        return Result.ok(safetyUser);

    }

    /**
     * 用户中心
     * 根据用户名查询
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            //return new ArrayList<>();
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            wrapper.eq(User::getUsername, username);
        }
        List<User> userList = userService.list(wrapper);
        List<User> list = userList.stream().map(user -> {
            user.setUserPassword(null);
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return Result.ok(list);

    }
    /**
     * 伙伴匹配首页推荐
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageNum, long pageSize,HttpServletRequest request) {
        User logUser = userService.getLogUser(request);
        String redisKey=String.format("user:recommend:%s",logUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //先查缓存
        Page<User> userPage =(Page<User>) valueOperations.get(redisKey);
        if(userPage!=null){
            return Result.ok(userPage);
        }
        //查数据库
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize),wrapper);

        //写缓存
        valueOperations.set(redisKey,userPage,300000, TimeUnit.MILLISECONDS);
            return Result.ok(userPage);

    }

    /**
     * 用户中心
     * 根据id删除用户
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new WxException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new WxException(ErrorCode.NULL_ERROR);
        }
        boolean b = userService.removeById(id);
        return Result.ok(b);

    }

    /**
     *伙伴匹配
     * 根据id和前端传过来的某个要更改的值更新用户
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if(user==null){
            throw new WxException(ErrorCode.PARAMS_ERROR);
        }
        User logUser = userService.getLogUser(request);
          int result =userService.updateUser(user,logUser);


        return Result.ok(result);

    }


    /**
     * 伙伴匹配
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new WxException(ErrorCode.PARAMS_ERROR, "标签列表”不能为空");
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return Result.ok(userList);
    }
}
