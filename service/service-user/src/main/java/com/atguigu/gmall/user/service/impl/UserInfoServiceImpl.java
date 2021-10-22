package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;
    /**
     * 查询用户的详细信息
     *
     * @param username
     * @return
     */
    @Override
    public UserInfo getUserInfo(String username) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getLoginName, username));
    }

    @Autowired
    private UserAddressMapper userAddressMapper;
    /**
     * 查询用户的收货地址列表
     *
     * @param username
     * @return
     */
    @Override
    public List<UserAddress> getUserAddress(String username) {
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, username));
    }
}
