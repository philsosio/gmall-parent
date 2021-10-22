package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * 商品详情的接口类
 */
public interface ItemService {

    /**
     * 获取商品详情的信息
     * @param skuId
     */
    public Map<String, Object> getGoodsDetail(Long skuId);
}
