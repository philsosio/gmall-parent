package com.atguigu.gmall.list.feign;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 搜索微服务的feign接口
 */
@FeignClient(name = "service-list", path = "/admin/list/")
public interface ListFeign {

    /**
     * 商品存入es
     * @param skuInfo
     * @return
     */
    @PostMapping(value = "/upper")
    public Result upper(@RequestBody SkuInfo skuInfo);

    /**
     * 从es中删除商品
     * @param id
     * @return
     */
    @GetMapping(value = "/del/{id}")
    public Result del(@PathVariable(value = "id")Long id);

    /**
     * 修改热度值
     * @param id
     * @return
     */
    @GetMapping(value = "/addHotScore/{id}")
    public Result addHotScore(@PathVariable(value = "id") Long id);

    /**
     * 搜索
     * @param searchData
     * @return
     */
    @GetMapping(value = "/search")
    public Map search(@RequestParam Map<String, String> searchData);
}
