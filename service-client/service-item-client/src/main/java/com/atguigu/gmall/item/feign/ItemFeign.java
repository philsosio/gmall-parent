package com.atguigu.gmall.item.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情微服务的feign接口类
 */
@FeignClient(name = "service-item", path = "/item")
public interface ItemFeign {

    /**
     * 获取商品的详情
     * @param id
     * @return
     */
    @GetMapping(value = "/getGoodsDetail/{id}")
    public Map getGoodsDetail(@PathVariable(value = "id") Long id);
}
