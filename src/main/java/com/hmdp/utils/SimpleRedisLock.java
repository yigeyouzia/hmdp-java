package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
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
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static  {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }


    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 获取锁
    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标志
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name,
                threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success); // 防止Boolean对象拆箱 空指针异常
    }


    @Override
    public void unlock() {
        // TODO 调用Lua脚本
        // 调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
    }

    // 释放锁
/*    @Override
    public void unlock() {
        //获取线程标志
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁中的标志
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
        //判断一致
        if(threadId.equals(id)) {
            stringRedisTemplate.delete(KEY_PREFIX + name);
            }
    }*/
}
