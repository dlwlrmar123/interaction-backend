package com.example.interact.service;

import com.example.interact.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("fakaString", "dog");
        valueOperations.set("fakaInt", 1);
        valueOperations.set("fakaDouble", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("faka");
        valueOperations.set("fakaUser", user);
        // 查
        Object faka = valueOperations.get("fakaString");
        Assertions.assertTrue("dog".equals((String) faka));
        faka = valueOperations.get("fakaInt");
        Assertions.assertTrue(1 == (Integer) faka);
        faka = valueOperations.get("fakaDouble");
        Assertions.assertTrue(2.0 == (Double) faka);
        System.out.println(valueOperations.get("fakaUser"));
        valueOperations.set("fakaString", "dog");
        redisTemplate.delete("fakaString");
    }
}
