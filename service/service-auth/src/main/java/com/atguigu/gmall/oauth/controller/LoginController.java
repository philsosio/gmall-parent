package com.atguigu.gmall.oauth.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.oauth.service.LoginService;
import com.atguigu.gmall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/user")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @PostMapping(value = "/login")
    public Result login(String username, String password){
        AuthToken authToken = loginService.login(username, password);
        //生成令牌成功以后,将令牌存入redis中去, key=当前申请令牌的IP地址,value就是令牌
        String ipAddress = IpUtil.getIpAddress(request);
        //存入redis
        redisTemplate.boundHashOps("user_login_ip").put(ipAddress, authToken.getAccessToken());
        //返回用户令牌信息
        return Result.ok(authToken);
    }
}
