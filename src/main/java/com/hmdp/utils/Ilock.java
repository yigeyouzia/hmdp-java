package com.hmdp.utils;

/**
 * @author cyt
 * @date 2023/9/16 21:13
 * @desc redis分布式锁
 */

public interface Ilock {
    /**
     *
     * @param timeoutSec 锁过期时间 自动释放
     * @return 是否获取成功
     */
    boolean tryLock(long timeoutSec);
    void unlock();
}
