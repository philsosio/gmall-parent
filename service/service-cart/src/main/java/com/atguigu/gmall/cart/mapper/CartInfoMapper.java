package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车表的mapper映射
 */
@Mapper
public interface CartInfoMapper extends BaseMapper<CartInfo> {
}
