package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;
import java.util.Map;

public interface CartInfoService {

    /**
     * 新增购物车
     * @param skuId
     * @param num
     * @param username
     * @return
     */
    public void addCart(Long skuId, Integer num, String username);

    /**
     * 修改商品的选中状态
     * @param skuId
     * @param username
     * @return
     */
    public void updateChecked(Long skuId, String username);

    /**
     * 查询用户的购物车数据
     * @param username
     * @return
     */
    public List<CartInfo> getCartInfo(String username);

    /**
     * 合并购物车数据
     * @param cartInfos
     * @param username
     */
    public void mergCartInfo(List<CartInfo> cartInfos, String username);


    /**
     * 获取订单的确认信息
     * @param username
     * @return
     */
    public Map<String, Object> getOrderConfirmInfo(String username);

    /**
     * 下单后移除购物车中的当前选中的商品信息
     * @param username
     */
    public void removeCartAfterAddOrder(String username);

}
