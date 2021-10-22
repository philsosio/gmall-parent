package com.atguigu.gmall.user.feign;

import com.atguigu.gmall.model.user.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户微服务的feign接口
 */
@FeignClient(name = "service-user", path = "/api/user/info")
public interface UserInfoFeign {


    /**
     * 根据用户名查询用户的详细信息
     * @param username
     * @return
     */
    @GetMapping(value = "/getUserInfo/{username}")
    public UserInfo getUserInfo(@PathVariable(value = "username") String username);

}
