package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品详情微服务需要使用的接口类
 */
public interface  ItemService{

    /**
     * 根据id查询sku的详细信息
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据id查询sku的详细信息
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoFromRedisOrDb(Long skuId);

    /**
     * 根据三级分类查询分类的详情
     * @param c3id
     * @return
     */
    public BaseCategoryView getCategory(Long c3id);

    /**
     * 查询图片的列表
     * @param skuId
     * @return
     */
    public List<SkuImage> getImageList(Long skuId);

    /**
     * 查询商品的价格
     * @param skuId
     * @return
     */
    public BigDecimal getPrice(Long skuId);

    /**
     * 根据sku和spu的id查询当前商品的所有的销售属性名称和值,并标注出当前的sku的销售属性是哪个
     * @param spuId
     * @param skuId
     * @return
     */
    public List<SpuSaleAttr> getSpuSaleAttrBySpuAndSku(Long spuId,
                                                       Long skuId);

    /**
     * 获取spu下所有sku的键值对
     * @param spuId
     * @return
     */
    public Map getSkuSaleAttrValueList(Long spuId);

    /**
     * 查询品牌详情
     * @param id
     * @return
     */
    public BaseTrademark getBaseTrademark(Long id);

    /**
     * 根据skuid查询平台属性名称和平台属性值
     * @param skuId
     * @return
     */
    public List<BaseAttrInfo> getBaseAttrInfoBySkuId(Long skuId);

    /**
     * 扣减库存
     * @param numMap
     */
    public Boolean decountStock(Map<String,Object> numMap);

    /**
     * 回滚库存
     * @param numMap
     */
    public Boolean rollbackStock(Map<String,Object> numMap);

}
