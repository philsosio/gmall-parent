package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import com.atguigu.gmall.user.util.GmallThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/user/info")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;


    /**
     * 根据用户名查询用户的详细信息
     * @param username
     * @return
     */
    @GetMapping(value = "/getUserInfo/{username}")
    public UserInfo getUserInfo(@PathVariable(value = "username") String username){
        return userInfoService.getUserInfo(username);
    }

    /**
     * 根据用户名查询用户的详细信息
     * @return
     */
    @GetMapping(value = "/getUserAddress")
    public Result<List<UserAddress>> getUserAddress(){
        String username = GmallThreadLocalUtils.getUserName();
        return Result.ok(userInfoService.getUserAddress(username));
    }
}
