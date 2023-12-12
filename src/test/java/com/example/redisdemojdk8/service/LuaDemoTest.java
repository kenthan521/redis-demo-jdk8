package com.example.redisdemojdk8.service;

import com.example.redisdemojdk8.RedisDemoJdk8ApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class LuaDemoTest extends RedisDemoJdk8ApplicationTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void luaDemoMethod() {

        String luaScript = readResourceFile("myscript.lua");

        RedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        List<String> keys = null; // 通常情况下，没有KEYS部分
        String[] args = new String[]{"10", "20"}; // 传递给Lua脚本的参数
        Long result = stringRedisTemplate.execute(script, keys, args);
        System.out.println(result);
    }

    private String readResourceFile(String fileName) {
        try (InputStream inputStream = LuaDemoTest.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}