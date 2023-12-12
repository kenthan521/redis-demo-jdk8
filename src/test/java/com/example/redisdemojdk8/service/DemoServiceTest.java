package com.example.redisdemojdk8.service;

import com.example.redisdemojdk8.RedisDemoJdk8ApplicationTests;
import com.example.redisdemojdk8.util.RedisUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DemoServiceTest extends RedisDemoJdk8ApplicationTests {

    @Autowired
    RedisUtils redisUtils;

    @Test
    public void demoMethod() {
        redisUtils.set("foo", "bar");
        redisUtils.setHash("key", "hkey", "hvalue");
    }

    @Test
    public void deleteAll() {
        redisUtils.flushDB();
    }

    @Test
    public void getVersion() {
        String mytype = redisUtils.getVersion("mytype2");
        System.out.println(mytype);
    }
}