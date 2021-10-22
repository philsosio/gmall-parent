package com.atguigu.gmall.pay.service;

import java.util.Map;

public interface AliPayService {

    /**
     * 支付宝支付
     * @param data
     * @return
     */
    public String aliPay(Map<String,String> data);


    /**
     * 获取支付的结果
     * @param orderId
     * @return
     */
    public String getPayResult(String orderId);
}
