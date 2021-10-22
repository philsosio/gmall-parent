package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.feign.ProductFeign;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.util.concurrent.AtomicDouble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增购物车
     *
     * @param skuId
     * @param num
     * @param username
     * @return
     */
//    @Async("abc")
    @Override
    public void addCart(Long skuId, Integer num, String username) {
        //参数校验
        if(skuId == null ||  num == null || StringUtils.isEmpty(username)){
            throw new RuntimeException("参数错误!");
        }
        //判断当前的操作中num是否为0
        if(num == 0){
            //删除mysql中购物车的数据
            cartInfoMapper.delete(
                    new LambdaQueryWrapper<CartInfo>()
                            .eq(CartInfo::getUserId, username)
                            .eq(CartInfo::getSkuId ,skuId));
            //删除redis中购物车的数据
            redisTemplate.boundHashOps("cart_info_" + username).delete(skuId + "");
            //返回
            return;
        }
        //查询购物车的信息,判断当前用户对于当前商品是否已经添加过了
        CartInfo cartInfo = cartInfoMapper.selectOne(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, username)
                        .eq(CartInfo::getSkuId, skuId));
        if(cartInfo != null){
            //合并购物车数据,并返回
            cartInfo.setSkuNum(cartInfo.getSkuNum() + num);
            //校验
            if(cartInfo.getSkuNum() <= 0){
                //删除mysql中购物车的数据
                cartInfoMapper.delete(
                        new LambdaQueryWrapper<CartInfo>()
                                .eq(CartInfo::getUserId, username)
                                .eq(CartInfo::getSkuId ,skuId));
                //删除redis中购物车的数据
                redisTemplate.boundHashOps("cart_info_" + username).delete(skuId + "");
                //返回
                return;
            }else{
                cartInfoMapper.updateById(cartInfo);
                redisTemplate.boundHashOps("cart_info_" + username).put(skuId+"", cartInfo);
                //返回
                return;
            }
        }
        if(num < 0){
            throw new RuntimeException("参数错误!");
        }
        //返回结果初始化
        cartInfo = new CartInfo();
        //查询商品的信息
        SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
        if(skuInfo == null || skuInfo.getId() == null){
            throw new RuntimeException("商品不存在!");
        }
        //构建购物车的对象
        cartInfo.setUserId(username);
        cartInfo.setSkuId(skuId);
        cartInfo.setCartPrice(skuInfo.getPrice());
        cartInfo.setSkuNum(num);
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuName(skuInfo.getSkuName());
        //保存购物车的数据到mysql中去
        int insert = cartInfoMapper.insert(cartInfo);
        //将购物车的数据还保存到redis中去
        if(insert <= 0){
            throw new RuntimeException("新增购物车失败,请重试!");
        }
        redisTemplate.boundHashOps("cart_info_" + username).put(skuId+"", cartInfo);
        //返回结果
        return;
    }

    /**
     * 修改商品的选中状态
     *
     * @param skuId
     * @param username
     * @return
     */
    @Override
    public void updateChecked(Long skuId, String username) {
        //参数校验
        if(skuId == null || StringUtils.isEmpty(username)){
            throw new RuntimeException("参数错误!");
        }
        //查询购物车信息
        CartInfo cartInfo = cartInfoMapper.selectOne(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, username)
                        .eq(CartInfo::getSkuId, skuId));
        if(cartInfo == null || cartInfo.getId() == null){
            throw new RuntimeException("购物车数据不存在!");
        }
        //修改购物车的选中状态
        cartInfo.setIsChecked(cartInfo.getIsChecked()==1?0:1);
        cartInfoMapper.updateById(cartInfo);
        redisTemplate.boundHashOps("cart_info_" + username).put(skuId+"", cartInfo);
        //返回结果
        return;
    }

    /**
     * 查询用户的购物车数据
     *
     * @param username
     * @return
     */
    @Override
    public List<CartInfo> getCartInfo(String username) {
        //先从redis查询数据
        List<CartInfo> cartInfoList = redisTemplate.boundHashOps("cart_info_" + username).values();
        if(!cartInfoList.isEmpty() && cartInfoList.size() > 0){
            return cartInfoList;
        }
        //从数据库查询数据
        return cartInfoMapper.selectList(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, username));
    }

    /**
     * 合并购物车数据
     *
     * @param cartInfoFromCookie
     * @param username
     */
    @Override
    public void mergCartInfo(List<CartInfo> cartInfoFromCookie, String username) {
        //参数校验
        if(cartInfoFromCookie.isEmpty() || StringUtils.isEmpty(username)){
            throw new RuntimeException("参数错误!");
        }
//        for (CartInfo cartInfo : cartInfoFromCookie) {
//            this.addCart(cartInfo.getSkuId(), cartInfo.getSkuNum(), username);
//        }
        //查询用户的数据库中所有购物车数据
        List<CartInfo> cartInfoListFromDb =
                cartInfoMapper.selectList(new LambdaQueryWrapper<CartInfo>().eq(CartInfo::getUserId, username));
        //循环合并
        for (CartInfo cartInfoCookie : cartInfoFromCookie) {
            //获取商品的id
            Long skuId = cartInfoCookie.getSkuId();
            Boolean flag = false;
            //和数据库中的id进行对比
            for (CartInfo cartInfoDb : cartInfoListFromDb) {
                //判断
                if(cartInfoDb.getSkuId().equals(skuId)){
                    //合并的操作
                    flag = true;
                    cartInfoDb.setSkuNum(cartInfoDb.getSkuNum() + cartInfoCookie.getSkuNum());
                    //更新
                    cartInfoMapper.updateById(cartInfoDb);
                    redisTemplate.boundHashOps("cart_info_" + username).put(skuId + "", cartInfoDb);
//                    this.addCart(skuId, cartInfoCookie.getSkuNum(), username);
                    break;
                }
            }
            //判断是否有一致的商品
            if(!flag){
                //新增
                this.addCart(skuId, cartInfoCookie.getSkuNum(), username);
            }
        }
    }

    /**
     * 获取订单的确认信息
     *
     * @param username
     * @return
     */
    @Override
    public Map<String, Object> getOrderConfirmInfo(String username) {
        //返回结果初始化
        Map<String, Object> result = new HashMap<>();
        //查询购物车中选中的商品的信息
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, username)
                        .eq(CartInfo::getIsChecked, 1));
        //初始化总数量
        AtomicInteger num = new AtomicInteger(0);
        //初始化总金额
        AtomicDouble money = new AtomicDouble(0);
        //遍历购物车中商品的信息,获取最新的商品价格
//        List<CompletableFuture> list = new ArrayList<>();
//        for (CartInfo cartInfo : cartInfoList) {
//            list.add(CompletableFuture.supplyAsync(() -> {
//                //获取最新的价格
//                BigDecimal price = productFeign.getPrice(cartInfo.getSkuId());
//                //设置最新的价格
//                cartInfo.setSkuPrice(price);
//                //累加数量
//                num.addAndGet(cartInfo.getSkuNum());
//                //累加金额
//                money.addAndGet(price.doubleValue() * cartInfo.getSkuNum());
//                //返回
//                return cartInfo;
//            }));
//        }
//        CompletableFuture.allOf( list.toArray(new CompletableFuture[list.size()])).join();
        //推荐方案
        List<CartInfo> cartInfoListNew = cartInfoList.parallelStream().map(cartInfo -> {
            //获取最新的价格
            BigDecimal price = productFeign.getPrice(cartInfo.getSkuId());
            //设置最新的价格
            cartInfo.setSkuPrice(price);
            //累加数量
            num.addAndGet(cartInfo.getSkuNum());
            //累加金额
            money.addAndGet(price.doubleValue() * cartInfo.getSkuNum());
            //返回
            return cartInfo;
        }).collect(Collectors.toList());
        //将数据返回
        result.put("num", num.get());
        result.put("money", money.get());
        result.put("cartInfoListNew", JSONObject.toJSONString(cartInfoListNew));
        return result;
    }

    /**
     * 下单后移除购物车中的当前选中的商品信息
     *
     * @param username
     */
    @Override
    public void removeCartAfterAddOrder(String username) {
        cartInfoMapper.delete(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, username)
                        .eq(CartInfo::getIsChecked, 1));
        //查询最新的购物车信息
        List<CartInfo> cartInfoList =
                cartInfoMapper.selectList(
                        new LambdaQueryWrapper<CartInfo>()
                                .eq(CartInfo::getUserId, username));
        //覆盖redis中的数据
        for (CartInfo cartInfo : cartInfoList) {
            redisTemplate.boundHashOps("cart_info_" + username).put(cartInfo.getSkuId() + "", cartInfo);
        }
    }
}
