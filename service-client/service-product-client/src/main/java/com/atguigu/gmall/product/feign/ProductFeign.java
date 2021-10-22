package com.atguigu.gmall.product.feign;

import com.atguigu.gmall.model.product.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品管理微服务的feign接口类
 */
@FeignClient(name = "service-product" ,path = "/api/product")
public interface ProductFeign {

    /**
     * 查询skuinfo的信息
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(value = "skuId") Long skuId);

    /**
     * 根据三级分类查询分类的详情
     * @param c3id
     * @return
     */
    @GetMapping(value = "/getCategory/{c3id}")
    public BaseCategoryView getCategory(@PathVariable(value = "c3id") Long c3id);

    /**
     * 查询skuinfo的图片信息
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSkuImage/{skuId}")
    public List<SkuImage> getSkuImage(@PathVariable(value = "skuId") Long skuId);

    /**
     * 查询skuinfo的价格
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getPrice/{skuId}")
    public BigDecimal getPrice(@PathVariable(value = "skuId") Long skuId);

    /**
     * 根据sku和spu的id查询当前商品的所有的销售属性名称和值,并标注出当前的sku的销售属性是哪个
     * @param spuId
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSpuSaleAttrBySpuAndSku/{spuId}/{skuId}")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuAndSku(@PathVariable(value = "spuId") Long spuId,
                                                       @PathVariable(value = "skuId") Long skuId);

    /**
     * 查询spu下所有sku的键值对
     * @param spuId
     * @return
     */
    @GetMapping(value = "/getSkuSaleAttrValueList/{spuId}")
    public Map getSkuSaleAttrValueList(@PathVariable(value = "spuId") Long spuId);

    /**
     * 根据品牌的id查询品牌的详情
     * @param id
     * @return
     */
    @GetMapping(value = "/getBaseTrademark/{id}")
    public BaseTrademark getBaseTrademark(@PathVariable(value = "id") Long id);

    /**
     * 根据skuid查询平台属性名称和平台属性值
     * @param id
     * @return
     */
    @GetMapping(value = "/getBaseAttrInfoBySkuId/{id}")
    public List<BaseAttrInfo> getBaseAttrInfoBySkuId(@PathVariable(value = "id") Long id);

    /**
     * 扣减库存
     * @param numMap
     * @return
     */
    @GetMapping(value = "/decountStock")
    public Boolean decountStock(@RequestParam Map<String, Object> numMap);

    /**
     * 回滚库存
     * @param numMap
     * @return
     */
    @GetMapping(value = "/rollbackStock")
    public Boolean rollbackStock(@RequestParam Map<String, Object> numMap);
}
