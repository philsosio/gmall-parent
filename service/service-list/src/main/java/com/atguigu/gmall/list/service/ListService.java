package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.product.SkuInfo;

public interface ListService {


    /**
     * 新增商品到es中去
     * @param skuInfo
     */
    public void addGoods(SkuInfo skuInfo);


    /**
     * 删除商品
     * @param id
     */
    public void delGoods(Long id);

    /**
     * 修改热度值
     * @param id
     */
    public void addHotScore(Long id);
}
