package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author cyt
 * @date 2023/9/16 21:13
 * @desc
 */
@SuppressWarnings({"all"})
public class SimpleRedisLock implements Ilock {

    private String name;
    private StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "lock:";

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 获取锁
    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标志
        long threadId = Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name,
                threadId + "", timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success); // 防止Boolean对象拆箱 空指针异常
    }


    // 释放锁
    @Override
    public void unlock() {
        stringRedisTemplate.delete(KEY_PREFIX + name);
    }
}
