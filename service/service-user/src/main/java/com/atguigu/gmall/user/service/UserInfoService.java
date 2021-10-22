package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;

import java.util.List;

public interface UserInfoService {

    /**
     * 查询用户的详细信息
     * @param username
     * @return
     */
    public UserInfo getUserInfo(String username);

    /**
     * 查询用户的收货地址列表
     * @param username
     * @return
     */
    public List<UserAddress> getUserAddress(String username);

}
