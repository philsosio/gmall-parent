package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.cart.util.GmallThreadLocalUtils;
import com.atguigu.gmall.cart.util.TokenUtil;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping(value = "/api/cart")
public class CartController {

    @Autowired
    private CartInfoService cartInfoService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 新增购物车
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping(value = "/addCartInfo")
    public Result addCartInfo(Long skuId, Integer num){
//        String username = "banzhang";
        String userName = GmallThreadLocalUtils.getUserName();
        CompletableFuture.runAsync(()->{
            cartInfoService.addCart(skuId, num, userName);
        }, threadPoolExecutor);
        return Result.ok();
    }

    /**
     * 修改购物车的选中状态
     * @param skuId
     * @return
     */
    @GetMapping(value = "/updateChecked")
    public Result updateChecked(Long skuId){
        String username = "banzhang";
        cartInfoService.updateChecked(skuId, username);
        return Result.ok();
    }

    /**
     * 查询购物车数据
     * @return
     */
    @GetMapping(value = "/getCartInfo")
    public Result getCartInfo(){
        String username = "banzhang";
        return Result.ok(cartInfoService.getCartInfo(username));
    }

    /**
     * 获取订单的确认信息
     * @return
     */
    @GetMapping(value = "/getOrderConfirmInfo")
    public Result getOrderConfirmInfo(){
        String userName = GmallThreadLocalUtils.getUserName();
        return Result.ok(cartInfoService.getOrderConfirmInfo(userName));
    }

    /**
     * 下单使用的购物车信息获取
     * @return
     */
    @GetMapping(value = "/getCartList")
    public Map<String, Object> getCartList(){
        String userName = GmallThreadLocalUtils.getUserName();
        return cartInfoService.getOrderConfirmInfo(userName);
    }

    /**
     * 下单后移除购车数据
     */
    @GetMapping(value = "/remove")
    public void remove(){
        String userName = GmallThreadLocalUtils.getUserName();
        cartInfoService.removeCartAfterAddOrder(userName);
    }
}
