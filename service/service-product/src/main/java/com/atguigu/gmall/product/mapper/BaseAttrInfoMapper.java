package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台属性的名称表mapper映射
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

    /**
     * 根据一级二级三级分类查询平台属性信息
     * @param category1
     * @param category2
     * @param category3
     * @return
     */
    public List<BaseAttrInfo> selectBaseAttrInfoByCategoryId(@Param("category1") Long category1,
                                                             @Param("category2") Long category2,
                                                             @Param("category3") Long category3);

    /**
     * 查询指定sku的平台属性名称和value的信息
     * @param skuId
     * @return
     */
    public List<BaseAttrInfo> selectBaseAttrInfoBySkuId(@Param("skuId") Long skuId);
}
