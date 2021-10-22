package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 品牌表mapper映射
 */
@Mapper
public interface BaseTradeMarkMapper extends BaseMapper<BaseTrademark> {

}
