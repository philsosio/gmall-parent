package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillOrderService;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/seckill/order")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 秒杀下单
     * @param time
     * @param id
     * @return
     */
    @GetMapping(value = "/addSeckillOrder")
    public Result addSeckillOrder(String time, String id){
        return Result.ok(seckillOrderService.addSeckillOrder(time, id));
    }

    /**
     * 主动取消秒杀订单
     * @return
     */
    @GetMapping(value = "/cancleOrder")
    public Result cancleOrder(){
        String username = "banzhang";
        seckillOrderService.cancleOrder(username, (short)1);
        return Result.ok();
    }
}
