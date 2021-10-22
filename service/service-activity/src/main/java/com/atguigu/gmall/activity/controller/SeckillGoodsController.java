package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.DateUtil;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/seckill/goods")
public class SeckillGoodsController {

    /**
     * 获取时间菜单
     * @return
     */
    @GetMapping(value = "/getTimeMenus")
    public Result getTimeMenus(){
        //获取当前时间段和后4个时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        //返回给前端
        List<String> menus = dateMenus.stream().map(data -> {
            return DateUtil.data2str(data, "yyyy-MM-dd HH:mm");
        }).collect(Collectors.toList());
        //返回
        return Result.ok(menus);
    }

    @Autowired
    private SeckillGoodsService seckillGoodsService;
    /**
     * 获取指定时间段的商品的列表
     * @param time
     * @return
     */
    @GetMapping(value = "/getSeckillGoods")
    public Result getSeckillGoods(String time){
        return Result.ok(seckillGoodsService.getSeckillGoods(time));
    }

    /**
     * 获取指定时间段的商品的详情
     * @param time
     * @return
     */
    @GetMapping(value = "/getSeckillGoodsDetail")
    public Result getSeckillGoodsDetail(String time, String id){
        return Result.ok(seckillGoodsService.getSeckillGoods(time, id));
    }
}
