package com.atguigu.gmall.oauth.service.impl;

import com.atguigu.gmall.oauth.service.LoginService;
import com.atguigu.gmall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public AuthToken login(String username, String password) {
        //参数校验
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            throw new RuntimeException("用户名密码不能为空!!!");
        }
        //包装参数body的参数一共三个---todo
        MultiValueMap<String, String> body = new HttpHeaders();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        //定义请求头---todo5
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Authorization", getHead());
        //发起post请求:http://localhost:9001/oauth/token
        ServiceInstance choose = loadBalancerClient.choose("service-oauth");
        String path = choose.getUri().toString();
        String url = path + "/oauth/token";
        //发起请求
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity(body, headers), Map.class);
        Map<String,String> resultMap = exchange.getBody();
        //初始化返回结果
        AuthToken authToken = new AuthToken();
        //获取令牌
        String accessToken = resultMap.get("access_token");
        authToken.setAccessToken(accessToken);
        //获取刷新令牌
        String refreshToken = resultMap.get("refresh_token");
        authToken.setRefreshToken(refreshToken);
        //获取jti
        String jti = resultMap.get("jti");
        authToken.setJti(jti);
        //返回结果
        return authToken;
    }

    /**
     * 获取请求头的参数的值
     * @return
     */
    private String getHead(){
        byte[] encode = Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes());
        return "Basic " + new String(encode);
    }
}
