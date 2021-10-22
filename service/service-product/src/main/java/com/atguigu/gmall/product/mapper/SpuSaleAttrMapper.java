package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * spu销售属性名称表的mapper映射
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    /**
     * 根据spu的id查询spu的销售属性列表
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> selectSpuSaleAttrBySpuId(@Param("spuId") Long spuId);

    /**
     * 根据sku和spu的id查询当前商品的所有的销售属性名称和值,并标注出当前的sku的销售属性是哪个
     * @param spuId
     * @param skuId
     * @return
     */
    public List<SpuSaleAttr> selectSpuSaleAttrBySpuIdAndSkuId(@Param("spuId") Long spuId,
                                                              @Param("skuId") Long skuId);
}
