package com.atguigu.gmall.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.pay.service.WxPayService;
import com.atguigu.gmall.pay.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class WxPayServiceImpl implements WxPayService {

    @Value("${weixin.pay.appid}")
    private String appId;

    @Value("${weixin.pay.partner}")
    private String partner;

    @Value("${weixin.pay.partnerkey}")
    private String partnerkey;

    @Value("${weixin.pay.notifyUrl}")
    private String notifyUrl;
    /**
     * 获取微信支付的支付url地址
     *
     * @param data
     * @return
     */
    @Override
    public String getPayUrl(Map<String,String> data) {
        //参数校验
        if(data == null || data.size() <=0){
            throw new RuntimeException("参数错误!");
        }
        //获取请求的url
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        //包装请求参数
        Map<String, String> map = new HashMap<>();
        map.put("appid", appId);
        map.put("mch_id", partner);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        map.put("body", "尚硅谷java0323测试商品");
        map.put("out_trade_no", data.get("orderId"));
        map.put("total_fee", data.get("money"));
        map.put("spbill_create_ip", "192.168.200.1");
        map.put("notify_url", notifyUrl);
        map.put("trade_type", "NATIVE");
        //构建附加数据
        Map<String, String> attchMap = new HashMap<>();
        attchMap.put("exchange", data.get("exchange"));
        attchMap.put("routingkey", data.get("routingkey"));
        if(StringUtils.isEmpty(data.get("username"))){
            attchMap.put("username", data.get("username"));
        }
        map.put("attach", JSONObject.toJSONString(attchMap));
        try {
            //发送请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            //设置参数xml格式的参数
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(map, partnerkey));
            //解析结果
            httpClient.post();
            //获取结果:结果为xml格式的字符串
            String content = httpClient.getContent();
            Map<String, String> result = WXPayUtil.xmlToMap(content);
            //判断这次请求是否正常,这次业务是否成功
            if(result.get("return_code").equals("SUCCESS")
                    && result.get("result_code").equals("SUCCESS")){
                //获取url的地址
                System.out.println(result);
                return result.get("code_url");
            }else{
                throw new RuntimeException("支付失败,请重试!");
            }
        }catch (Exception e){
            throw new RuntimeException("支付失败,请重试!");
        }
    }

    /**
     * 主动获取支付的结果
     *
     * @param orderId
     * @return
     */
    @Override
    public Map getPayResult(String orderId) {
        //参数校验
        if(orderId == null){
            throw new RuntimeException("参数错误!");
        }
        //获取请求的url
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        //包装请求参数
        Map<String, String> map = new HashMap<>();
        map.put("appid", appId);
        map.put("mch_id", partner);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        map.put("out_trade_no", orderId);
        try {
            //发送请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            //设置参数xml格式的参数
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(map, partnerkey));
            //解析结果
            httpClient.post();
            //获取结果:结果为xml格式的字符串
            String content = httpClient.getContent();
            Map<String, String> result = WXPayUtil.xmlToMap(content);
            //判断这次请求是否正常,这次业务是否成功
            if(result.get("return_code").equals("SUCCESS")
                    && result.get("result_code").equals("SUCCESS")){
                return result;
            }else{
                throw new RuntimeException("查询失败,请重试!");
            }
        }catch (Exception e){
            throw new RuntimeException("查询失败,请重试!");
        }
    }

    public static void main(String[] args) throws Exception{
        Map<String, String> map = new HashMap<>();
        map.put("id","1");
        map.put("name","张三");
        map.put("address","深圳");
        String s = WXPayUtil.generateSignedXml(map, "123");
        System.out.println(s);
        String s1 = WXPayUtil.generateNonceStr();
        System.out.println(s1);
    }
}
