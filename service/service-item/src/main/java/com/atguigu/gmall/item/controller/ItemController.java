package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 获取商品的详情
     * @param id
     * @return
     */
    @GetMapping(value = "/getGoodsDetail/{id}")
    public Map getGoodsDetail(@PathVariable(value = "id") Long id){
        return itemService.getGoodsDetail(id);
    }
}
