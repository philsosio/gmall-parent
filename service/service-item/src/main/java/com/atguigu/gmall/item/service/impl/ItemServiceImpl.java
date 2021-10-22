package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.feign.ListFeign;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 商品详情的实现类
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ListFeign listFeign;

    /**
     * 获取商品详情的信息
     *
     * @param skuId
     */
    @Override
    public Map<String, Object> getGoodsDetail(Long skuId) {
        //返回结果的初始化
        Map<String, Object> result = new HashMap<>();
        //参数校验
        if(skuId == null){
            throw new RuntimeException("参数错误!!!!!");
        }
        //获取商品的基本信息SkuInfo--任务一--核心任务
        CompletableFuture<SkuInfo> future1 = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
            result.put("skuInfo", skuInfo);
            System.out.println("任务1" + Thread.currentThread().getName());
            return skuInfo;
        }, threadPoolExecutor);
        //获取商品的分类信息category--任务二
        CompletableFuture<Void> future2 = future1.thenAcceptAsync((skuInfo -> {
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView category = productFeign.getCategory(category3Id);
            result.put("category", category);
            System.out.println("任务2" + Thread.currentThread().getName());
        }), threadPoolExecutor);
        //获取商品的图片列表List<SkuImgage>--任务三
        CompletableFuture<Void> future3 = future1.thenAcceptAsync((skuInfo -> {
            List<SkuImage> skuImage = productFeign.getSkuImage(skuId);
            result.put("skuImage", skuImage);
            System.out.println("任务3" + Thread.currentThread().getName());
        }), threadPoolExecutor);
        //获取商品的价格信息price--任务四
        CompletableFuture<Void> future4 = future1.thenAcceptAsync((skuInfo -> {
            BigDecimal price = productFeign.getPrice(skuId);
            result.put("price", price);
            System.out.println("任务4" + Thread.currentThread().getName());
        }), threadPoolExecutor);
        //获取该sku所属的spu的全部的销售属性和销售属性的值SaleAttrInfo---任务五
        CompletableFuture<Void> future5 = future1.thenAcceptAsync((skuInfo -> {
            // &获取当前sku的销售属性和销售属性的值SkuSaleAttrValue
            Long spuId = skuInfo.getSpuId();
            List<SpuSaleAttr> spuSaleAttrs = productFeign.getSpuSaleAttrBySpuAndSku(spuId, skuId);
            result.put("spuSaleAttrs", spuSaleAttrs);
            System.out.println("任务5" + Thread.currentThread().getName());
        }), threadPoolExecutor);
        //获取当前spu下所有sku的销售属性的键值对Map--任务六
        CompletableFuture<Void> future6 = future1.thenAcceptAsync((skuInfo -> {
            Map skuSaleAttrMap = productFeign.getSkuSaleAttrValueList(skuInfo.getSpuId());
            result.put("skuSaleAttrMap", skuSaleAttrMap);
            System.out.println("任务6" + Thread.currentThread().getName());
        }), threadPoolExecutor);
        //等待全部的任务执行完成以后,才能进行返回
        CompletableFuture.allOf(future1, future2, future3, future4 ,future5, future6).join();
        //热度值更新
        CompletableFuture.runAsync(()->{
            listFeign.addHotScore(skuId);
        });
        //返回结果Map
        return result;
    }
}
