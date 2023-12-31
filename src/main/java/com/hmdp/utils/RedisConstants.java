package com.hmdp.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 360000L;
    public static final String LOGIN_TOKEN_NAME = "authorization";

    public static final Long CACHE_NULL_TTL = 2L; // 缓存穿透null时间

    public static final Long CACHE_SHOP_TTL = 30L; // 店铺（id）缓存时间
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final String CACHE_SHOP_LIST = "cache:shop:list";

    public static final String LOCK_SHOP_KEY = "lock:shop:"; // 店铺互斥锁
    public static final Long LOCK_SHOP_TTL = 10L; // 店铺互斥锁 过期时间
    public static final Long SIMPLE_REDIS_LOCK_DDL = 1200L; // redis分布式锁 过期时间

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
