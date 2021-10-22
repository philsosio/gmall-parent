package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.dao.GoodsRepository;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ProductFeign productFeign;
    /**
     * 新增商品到es中去
     *
     * @param skuInfo
     */
    @Override
    public void addGoods(SkuInfo skuInfo) {
        //创建goods对象
        Goods goods = new Goods();
        //转换数据skuinfo---->Goods
        goods.setId(skuInfo.getId());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setCreateTime(new Date());
        //品牌信息补充
        BaseTrademark baseTrademark = productFeign.getBaseTrademark(skuInfo.getTmId());
        goods.setTmId(baseTrademark.getId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        //待补充一级二级三级分类的id和名字
        BaseCategoryView category = productFeign.getCategory(skuInfo.getCategory3Id());
        goods.setCategory1Id(category.getCategory1Id());
        goods.setCategory1Name(category.getCategory1Name());
        goods.setCategory2Id(category.getCategory2Id());
        goods.setCategory2Name(category.getCategory2Name());
        goods.setCategory3Id(category.getCategory3Id());
        goods.setCategory3Name(category.getCategory3Name());
        //待补充sku的平台属性信息
        List<BaseAttrInfo> baseAttrInfoList = productFeign.getBaseAttrInfoBySkuId(skuInfo.getId());
        List<SearchAttr> searchAttrs = baseAttrInfoList.stream().map(attr -> {
            //创建对象
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(attr.getId());
            searchAttr.setAttrName(attr.getAttrName());
            searchAttr.setAttrValue(attr.getAttrValueList().get(0).getValueName());
            //返回
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrs);
        //保存数据
        goodsRepository.save(goods);
    }

    /**
     * 删除商品
     *
     * @param id
     */
    @Override
    public void delGoods(Long id) {
        goodsRepository.deleteById(id);
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 修改热度值
     *
     * @param id
     */
    @Override
    public void addHotScore(Long id) {
        //参数校验
        if(id == null){
            return;
        }
        //查询es中商品的数据
        Goods goods = goodsRepository.findById(id).get();
        if(goods == null || goods.getId() == null){
            return;
        }
        //修改商品的热度值,将商品的热度值保存在redis中
        Double score =
                redisTemplate.boundZSetOps("goods_hot_score").incrementScore(id + "", 1);
        //每10次同步一次数据到es中去
        if(score % 10 == 0){
            goods.setHotScore(score.longValue());
            goodsRepository.save(goods);
        }
    }
}
