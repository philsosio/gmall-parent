package com.atguigu.gmall.pay.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.pay.service.AliPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/pay/ali")
public class AliPayController {

    @Autowired
    private AliPayService aliPayService;

    /**
     * 支付宝支付
     * @param data
     * @return
     */
    @GetMapping(value = "/getPay")
    public String getPay(@RequestParam Map<String,String> data){
        return aliPayService.aliPay(data);
    }

    /**
     * 查询支付结果
     * @param orderId
     * @return
     */
    @GetMapping(value = "/getPayResult")
    public String getPayResult(String orderId){
        return aliPayService.getPayResult(orderId);
    }



    /**
     * 同步回调
     * @return
     */
    @RequestMapping(value = "/callback/return")
    public String callbackReturn(@RequestParam Map<String,String> data){
        return "success";
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 异步回调
     * @return
     */
    @RequestMapping(value = "/callback/notify")
    public String callbacknotify(@RequestParam Map<String,String> data){
        String passbackParams = data.get("passback_params");
        Map<String,String> map = JSONObject.parseObject(passbackParams, Map.class);
        //发送支付结果的消息
        rabbitTemplate.convertAndSend(map.get("exchange"), map.get("routingkey"), JSONObject.toJSONString(data));
        return "success";
    }
}
