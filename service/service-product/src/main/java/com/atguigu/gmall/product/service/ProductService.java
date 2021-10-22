package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 后台管理使用的api接口类
 */
public interface ProductService {

    /**
     * 查询所有的一级分类
     * @return
     */
    public List<BaseCategory1> findCategory1();

    /**
     * 根据一级分类查询二级分类
     * @param id
     * @return
     */
    public List<BaseCategory2> findCategory2(Long id);

    /**
     * 根据二级分类查询三级分类
     * @param id
     * @return
     */
    public List<BaseCategory3> findCategory3(Long id);


    /**
     * 保存平台属性
     * @param baseAttrInfo
     * @return
     */
    public BaseAttrInfo saveBaseAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据一级二级三级分类查询平台属性信息
     * @param category1
     * @param category2
     * @param category3
     * @return
     */
    public List<BaseAttrInfo> getBaseAttrInfoList(Long category1,
                                                  Long category2,
                                                  Long category3);

    /**
     * 根据平台属性id查询平台属性值
     * @param id
     * @return
     */
    public List<BaseAttrValue> getBaseAttrValue(Long id);

    /**
     * 查询品牌列表
     * @return
     */
    public List<BaseTrademark> getBaseTrademarkList();


    /**
     * 查询销售属性列表
     * @return
     */
    public List<BaseSaleAttr> baseSaleAttrList();

    /**
     * 新增spu
     * @param spuInfo
     */
    public SpuInfo saveSpu(SpuInfo spuInfo);

    /**
     * 根据三级分类id查询spu的列表信息
     * @param cid
     * @param page
     * @param size
     * @return
     */
    public IPage<SpuInfo> findSpuByCategory3Id(Long cid, Integer page, Integer size);

    /**
     * 根据spu的id查询spu的图片列表
     * @param id
     * @return
     */
    public List<SpuImage> findBySpuId(Long id);

    /**
     * 根据spu的id查询spu的销售属性列表
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(Long spuId);

    /**
     * 新增/修改sku
     * @param skuInfo
     * @return
     */
    public SkuInfo saveSkuInfo(SkuInfo skuInfo);

    /**
     * sku的分页查询
     * @param page
     * @param size
     * @return
     */
    public IPage<SkuInfo> getSkuInfo(Integer page, Integer size);

    /**
     * 商品上下架: 0--下架 1---上架
     * @param skuId
     * @param status
     */
    public void updateSkuInfo(Long skuId, Short status);

    /**
     * 查询首页的分类信息列表
     * @return
     */
    public List<JSONObject> getIndexCategory();
}
