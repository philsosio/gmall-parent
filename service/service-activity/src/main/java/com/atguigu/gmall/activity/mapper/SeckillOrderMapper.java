package com.atguigu.gmall.activity.mapper;

import com.atguigu.gmall.activity.pojo.SeckillOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀订单的mapper映射
 */
@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
}
