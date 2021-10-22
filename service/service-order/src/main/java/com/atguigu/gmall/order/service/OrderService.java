package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

import java.util.Map;

public interface OrderService {

    /**
     * 新增订单
     * @param orderInfo
     * @return
     */
    public OrderInfo addOrder(OrderInfo orderInfo);

    /**
     * 取消订单
     * @param id
     * @param status: 0-主动取消 1-超时取消
     */
    public void cancleOrder(Long id, Short status);

    /**
     * 修改订单的状态
     * @param dataMap
     * @param status:0-微信 1-支付宝
     */
    public void updateOrder(Map<String, String> dataMap, Short status);
}
