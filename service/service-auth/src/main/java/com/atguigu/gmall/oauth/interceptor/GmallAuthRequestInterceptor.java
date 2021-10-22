package com.atguigu.gmall.oauth.interceptor;

import com.atguigu.gmall.oauth.util.AdminJwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GmallAuthRequestInterceptor implements RequestInterceptor {

    /**
     * feign发起远程调用前拦截请求,在请求头中添加临时令牌,访问用户信息
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //创建令牌信息
        String token ="Bearer "+ AdminJwtUtil.adminJwt();
        //将令牌添加到头文件中
        requestTemplate.header("Authorization", token);
    }
}