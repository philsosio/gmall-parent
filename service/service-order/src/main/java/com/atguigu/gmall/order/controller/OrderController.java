package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.constant.OrderConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 生成订单确认页面的流水号
     * @return
     */
    @GetMapping(value = "/getWareId")
    public Result getWareId(){
        String wareId = UUID.randomUUID().toString().replace("-","");
        redisTemplate.boundValueOps(wareId).increment(1);
        return Result.ok(wareId);
    }

    /**
     * 新增订单
     * @param orderInfo
     * @return
     */
    @PostMapping(value = "/addOrder")
    public Result addOrder(@RequestBody OrderInfo orderInfo){
        //确认流水号
        String wareId = orderInfo.getWareId();
        //流水号为空,非法请求
        if(StringUtils.isEmpty(wareId)){
            return Result.fail("流水号错误!!");
        }
        //流水号不为空,用户乱填的流水号
        Long increment = redisTemplate.boundValueOps(wareId).increment(1);
        if(increment.intValue() != 2){
            return Result.fail("流水号错误!!");
        }
        //补全用户名
        orderInfo.setUserId("banzhang");
        orderInfo = orderService.addOrder(orderInfo);
        //删除流水号
        redisTemplate.delete(wareId);
        //新增返回
        return Result.ok(orderInfo);
    }

    /**
     * 新增订单
     * @param orderInfo
     * @return
     */
    @PostMapping(value = "/addOrder1")
    public Result addOrder1(@RequestBody OrderInfo orderInfo){
        String username = "bangzhang";
        //获取用户的下单标识
        Long increment = redisTemplate.boundValueOps("user_add_order_flag_" + username).increment(1);
        if(increment.intValue() > 1){
            return Result.fail("正在处理其他订单,请稍等重试!!");
        }
        //补全用户名
        orderInfo.setUserId(username);
        orderInfo = orderService.addOrder(orderInfo);
        //删除流水号
        redisTemplate.delete("user_add_order_flag_" + username);
        //新增返回
        return Result.ok(orderInfo);
    }

    /**
     * 主动取消订单
     * @param id
     * @return
     */
    @GetMapping(value = "/cancleOrder/{id}")
    public Result cancleOrder(@PathVariable(value = "id") Long id){
        //主动取消订单
        orderService.cancleOrder(id, OrderConst.CACNCLE);
        return Result.ok();
    }
}
