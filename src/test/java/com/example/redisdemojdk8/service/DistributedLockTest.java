package com.example.redisdemojdk8.service;

import com.example.redisdemojdk8.RedisDemoJdk8ApplicationTests;
import com.example.redisdemojdk8.util.RedisUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.*;

/**
 * Redis 做分布式锁
 */
public class DistributedLockTest extends RedisDemoJdk8ApplicationTests {

    @Autowired
    Jedis jedis;

    @Test
    public void demoMethod() throws InterruptedException {
        String key = "mylock";
        boolean gotLock1 = getLock(key);
        if (gotLock1) {
            System.out.println("第一次获取到锁");
        }

        Thread.sleep(1000);

        boolean isLock2 = getLock(key);
        if (isLock2) {
            System.out.println("第二次获取到锁");
        }
    }

    private boolean getLock(String key) {
        Long flag = jedis.setnx(key, "1");
        if (flag == 1) {
            jedis.expire(key, 10);
        }
        return flag == 1;
    }

    private void releaseLock(String key) {
        jedis.del(key);
    }

}