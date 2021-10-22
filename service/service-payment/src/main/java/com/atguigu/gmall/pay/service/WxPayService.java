package com.atguigu.gmall.pay.service;

import java.util.Map;

public interface WxPayService {

    /**
     * 获取微信支付的支付url地址
     * @param data
     * @return
     */
    public String getPayUrl(Map<String,String> data);

    /**
     * 主动获取支付的结果
     * @param orderId
     * @return
     */
    public Map getPayResult(String orderId);
}
