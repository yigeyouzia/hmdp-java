package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@SuppressWarnings({"ALL"})
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        // 解决缓存穿透
        // Shop shop = queryWithPassThrough(id);
        // 解决缓存击穿
        Shop shop = queryWithMutex(id);
        if (shop == null) {
            return Result.fail("查询店铺失败😥😥");
        }
        return Result.ok(shop);
    }

    // 缓存击穿封装
    public Shop queryWithMutex(Long id) {
        String CacheShopKey = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CacheShopKey);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.存在 直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 缓存穿透：命中是否空值
        if (shopJson != null) { // 为 “” 空字符串
            // 返回错误
            return null;
        }
        // TODO 4.不存在 实现缓存重建
        String lockKey = null;
        Shop shop = null;
        try {
            // 4.1获取互斥锁
            lockKey = RedisConstants.LOCK_SHOP_KEY + id;
            boolean isLock = tryLock(lockKey);
            // 4.2获取失败 休眠重试
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(id); // 递归重试
            }
            // 4.3获取锁成功 根据id查询数据库
            shop = getById(id);
            // 模拟重建延迟********************
            Thread.sleep(200);
            // 5 shop不存在
            if (shop == null) {
                // 解决缓存穿透 存入空值
                stringRedisTemplate.opsForValue().set(CacheShopKey, "",
                        RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 6. shop存在 写入redis 返回
            String shop2Json = JSONUtil.toJsonStr(shop);
            stringRedisTemplate.opsForValue().set(CacheShopKey, shop2Json,
                    RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7.释放互斥锁
            unlock(lockKey);
        }
        return shop;
    }

    // 缓存穿透封装
    public Shop queryWithPassThrough(Long id) {
        String CacheShopKey = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CacheShopKey);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.存在 直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 缓存穿透：命中是否空值
        if (shopJson != null) { // 为 “” 空字符串
            // 返回错误
            return null;
        }
        // 4.不存在 根据id 查数据库
        Shop shop = getById(id);
        // 4.1.不存在 返回错误
        if (shop == null) {
            // 解决缓存穿透 存入空值
            stringRedisTemplate.opsForValue().set(CacheShopKey, "",
                    RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 4.2 存在 写入redis 返回
        String shop2Json = JSONUtil.toJsonStr(shop);
        stringRedisTemplate.opsForValue().set(CacheShopKey, shop2Json, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }

    // 互斥锁  获得锁
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    // 互斥锁  释放锁
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    // 更新缓存 一致性操作
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long shopId = shop.getId();
        if (shopId == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1.更新数据库
        updateById(shop);
        // 2.删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shopId);
        return Result.ok();
    }
}
