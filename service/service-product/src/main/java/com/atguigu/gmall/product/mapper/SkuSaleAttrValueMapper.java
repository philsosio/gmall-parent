package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性表的mapper映射
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    /**
     * 查询键值对
     * @param spuId
     * @return
     */
    @Select("SELECT sku_id,GROUP_CONCAT( DISTINCT sale_attr_value_id ORDER BY sale_attr_value_id SEPARATOR ',' ) AS sku_sale_values " +
            "FROM sku_sale_attr_value " +
            "WHERE spu_id = #{spuId} " +
            "GROUP BY sku_id")
    public List<Map> getSkuSaleAttrValueKeysBySpuId(@Param("spuId") Long spuId);
}
