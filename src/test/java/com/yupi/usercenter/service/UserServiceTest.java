package com.yupi.usercenter.service;
import java.util.Date;

import com.yupi.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Test
    public void testAddUser() {
        User user = new User();

        user.setUsername("wxxx");
        user.setUserAccount("12346");
        user.setAvatarUrl("https://i1.hdslb.com/bfs/face/a1e1eec29314a6f0ed7ad59bf3f573ece5db5c71.jpg@240w_240h_1c_1s_!web-avatar-space-header.avif");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("3435");

userService.save(user);

    }

    @Test
    void userRegister() {

            //测试密码为空
            String userAccount = "mark";
            String userPassword = "";
            String checkPassword = "12345678";
            String planetCode="1";
            long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
            Assert.assertEquals(-1, result);

            //测试账号小于4位
            userAccount = "la";
            userPassword = "12345678";
            result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
            Assert.assertEquals(-1, result);

            //测试密码小于8位
            userAccount = "mark";
            userPassword = "123456";
            checkPassword = "123456";
            result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
            Assert.assertEquals(-1, result);

            //测试账号含有特殊字符
            userAccount = "mark@ :;";
            result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
            Assert.assertEquals(-1, result);

            //测试密码和校验密码不相同
            userAccount = "mark";
            userPassword = "12345678";
            checkPassword = "1234568889910";
            result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
            Assert.assertEquals(-1, result);

            //测试账户名重复
            userAccount = "CatMark";
            userPassword = "12345678";
            checkPassword = "12345678";
            result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
            Assert.assertEquals(-1, result);

            //测试是否可以注册成功
            userAccount = "mark";
            userPassword = "12345678";
            checkPassword = "12345678";
            result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assert.assertEquals(-1, result);
        }

    @Test
    void doLogin() {
         String userAccount = "CatMark";
        String  userPassword = "12345678";
        //userService.doLogin(userAccount,)
    }
}
