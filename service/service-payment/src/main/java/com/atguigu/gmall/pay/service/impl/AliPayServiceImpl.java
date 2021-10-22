package com.atguigu.gmall.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Value("${alipay_url}")
    private String url;

    @Value("${app_id}")
    private String appId;

    @Value("${app_private_key}")
    private String appPrivateKey;

    @Value("${alipay_public_key}")
    private String alipayPublicKey;

    @Value("${return_payment_url}")
    private String returnPaymentUrl;

    @Value("${notify_payment_url}")
    private String notifyPaymentUrl;
    /**
     * 支付宝支付
     *
     * @param orderId
     * @param money
     * @return
     */
    @Override
    public String aliPay(Map<String,String> data) {
        //客户端初始化
        AlipayClient alipayClient =
                new DefaultAlipayClient(url,
                        appId,
                        appPrivateKey,
                        "json",
                        "utf-8",
                        alipayPublicKey,
                        "RSA2");
        //构建request
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //设置通知的地址
        request.setNotifyUrl(notifyPaymentUrl);
        //设置同步回调的地址
        request.setReturnUrl(returnPaymentUrl);
        //设置请求参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", data.get("orderId"));
        bizContent.put("total_amount", data.get("money"));
        bizContent.put("subject", "尚硅谷阿里支付java0323测试商品!");
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //包装附加参数
        Map<String, String> attchMap = new HashMap<>();
        attchMap.put("exchange", data.get("exchange"));
        attchMap.put("routingkey", data.get("routingkey"));
        if(StringUtils.isEmpty(data.get("username"))){
            attchMap.put("username", data.get("username"));
        }
        bizContent.put("passback_params", JSONObject.toJSONString(attchMap));
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if(response.isSuccess()){
                return response.getBody();
            } else {
                throw new RuntimeException("调用失败!请重试!");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("调用失败!请重试!");
        }
    }

    /**
     * 获取支付的结果
     *
     * @param orderId
     * @return
     */
    @Override
    public String getPayResult(String orderId) {
        //客户端初始化
        AlipayClient alipayClient =
                new DefaultAlipayClient(url,
                        appId,
                        appPrivateKey,
                        "json",
                        "utf-8",
                        alipayPublicKey,
                        "RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                return response.getBody();
            } else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
