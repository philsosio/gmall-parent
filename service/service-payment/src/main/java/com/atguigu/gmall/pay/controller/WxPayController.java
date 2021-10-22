package com.atguigu.gmall.pay.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.pay.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/pay/wx")
public class WxPayController {

    @Autowired
    private WxPayService wxPayService;

    /**
     * 获取支付的地址
     * @param data
     * @return
     */
    @GetMapping(value = "/getPayUrl")
    public Result getPayUrl(@RequestParam Map<String,String> data){
        return Result.ok(wxPayService.getPayUrl(data));
    }

    /**
     * 查询支付的结果
     * @param orderId
     * @return
     */
    @GetMapping(value = "/getPayResult")
    public Result getPayResult(String orderId){
        return Result.ok(wxPayService.getPayResult(orderId));
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 微信回调的通知地址
     */
    @RequestMapping(value = "/callback/notify")
    public String wxnotify(HttpServletRequest request) throws Exception{
        //获取支付的结果:输入流
        ServletInputStream is = request.getInputStream();
        //输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //输入流转输出流
        byte[] buffer = new byte[1024];
        int len;
        while ( (len = is.read(buffer)) != -1){
            os.write(buffer, 0, len);
        }
        //输出流转换为字符串
        String xmlReturn = new String(os.toByteArray());
        //将xml格式的字符串转换为map
        Map<String, String> mapReturn = WXPayUtil.xmlToMap(xmlReturn);
        //获取附加参数
        String attach = mapReturn.get("attach");
        Map<String,String> map = JSONObject.parseObject(attach, Map.class);
        //发送支付结果的消息
        rabbitTemplate.convertAndSend(map.get("exchange"), map.get("routingkey"), JSONObject.toJSONString(mapReturn));
        //响应微信,收到了支付的结果
        Map<String,String> result = new HashMap<>();
        result.put("return_code", "SUCCESS");
        result.put("return_msg", "OK");
        return WXPayUtil.mapToXml(result);
    }
}
