package com.example.redisdemojdk8.service;

import com.example.redisdemojdk8.RedisDemoJdk8Application;
import com.example.redisdemojdk8.RedisDemoJdk8ApplicationTests;
import com.example.redisdemojdk8.util.RedisUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

import static org.junit.Assert.*;

public class DemoServiceTest extends RedisDemoJdk8ApplicationTests {

    @Autowired
    RedisUtils redisUtils;

    @Test
    public void demoMethod() {
        redisUtils.set("foo", "bar");
    }
}