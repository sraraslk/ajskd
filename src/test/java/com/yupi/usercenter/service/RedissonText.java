package com.yupi.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonText {
    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test() {

        RList<Object> rList = redissonClient.getList("wxxx");
       // rList.add("1");
        rList.remove(0);


    }


}
