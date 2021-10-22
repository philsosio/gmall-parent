package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/test")
@RestController
public class TestController {

    @Autowired
    private TestService testService;


    /**
     * 测试往redis中写值
     * @return
     */
    @GetMapping
    public Result test(){
        testService.setValueRedission();
        //返回结果
        return Result.ok();
    }
}
