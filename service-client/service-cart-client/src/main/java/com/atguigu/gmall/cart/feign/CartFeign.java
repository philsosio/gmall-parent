package com.atguigu.gmall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * 购物车微服务的feign接口
 */
@FeignClient(name = "service-cart", path = "/api/cart")
public interface CartFeign {

    /**
     * 下单使用的购物车信息获取
     * @return
     */
    @GetMapping(value = "/getCartList")
    public Map<String, Object> getCartList();

    /**
     * 下单后移除购车数据
     */
    @GetMapping(value = "/remove")
    public void remove();
}
