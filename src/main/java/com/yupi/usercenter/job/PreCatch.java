package com.yupi.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PreCatch {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserService userService;

    private List<Long> mainUserIdList= Arrays.asList(1L);

    @Scheduled(cron = "0 1 17 * * ? ")
    public void preCatch(){
        for (Long userId : mainUserIdList) {
            String redisKey=String.format("user:recommend:%s",userId);
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            Page<User> userPage = userService.page(new Page<>(1,20),wrapper);
            valueOperations.set(redisKey,userPage,300000, TimeUnit.MILLISECONDS);
        }

        System.out.println("定时任务执行了");
    }

}
