package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.pojo.SeckillGoodsNew;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取指定时间段的秒杀商品列表
     *
     * @param time
     * @return
     */
    @Override
    public List<SeckillGoodsNew> getSeckillGoods(String time) {
        return redisTemplate.boundHashOps("seckillGoods_" + time).values();
    }

    /**
     * 获取指定时间段的秒杀商品列表
     *
     * @param time
     * @param id
     * @return
     */
    @Override
    public SeckillGoodsNew getSeckillGoods(String time, String id) {
        return (SeckillGoodsNew)redisTemplate.boundHashOps("seckillGoods_" + time).get(id);
    }
}
