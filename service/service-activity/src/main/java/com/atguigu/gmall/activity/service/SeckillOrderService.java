package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.activity.pojo.UserRecode;

import java.util.Map;

public interface SeckillOrderService {

    /**
     * 秒杀排队
     * @param time
     * @param id
     * @return
     */
    public UserRecode addSeckillOrder(String time, String id);

    /**
     * 秒杀订单的取消
     * @param username
     * @param status: 1-主动取消 0-被动取消
     */
    public void cancleOrder(String username, Short status);

    /**
     * 修改秒杀订单
     * @param map
     * @param status: 1-ali 0-微信
     */
    public void updateSeckillOrder(Map<String,String> map, Short status);
}
