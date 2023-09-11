package com.hmdp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    // TODO 店铺信息redis缓存
    @Override
    public Result queryTypeList() {
        String CacheList = RedisConstants.CACHE_SHOP_LIST;
        // 查redis
        List<String> list = stringRedisTemplate.opsForList().range(CacheList, 0, 10);
        // cache命中
        if (CollectionUtil.isNotEmpty(list)) {
            List<ShopType> shopTypeList = list.stream().
                    map(shopJson -> JSONUtil.toBean(shopJson, ShopType.class))
                    .collect(Collectors.toList());
            return Result.ok(shopTypeList);
        }
        // 查mysql
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList == null) {
            return Result.fail("店铺列表查询失败");
        }
        List<String> shop2Json = typeList.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList());
        // 写入redis
        stringRedisTemplate.opsForList().leftPush(CacheList, String.valueOf(shop2Json));
        return Result.ok(typeList);
    }
}
