package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.activity.pojo.SeckillGoodsNew;

import java.util.List;

public interface SeckillGoodsService {

    /**
     * 获取指定时间段的秒杀商品列表
     * @param time
     * @return
     */
    public List<SeckillGoodsNew> getSeckillGoods(String time);

    /**
     * 获取指定时间段的秒杀商品列表
     * @param time
     * @return
     */
    public SeckillGoodsNew getSeckillGoods(String time, String id);
}
