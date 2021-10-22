package com.atguigu.gmall.activity.mapper;

import com.atguigu.gmall.activity.pojo.SeckillGoodsNew;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 秒杀商品表的映射
 */
@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoodsNew> {


    /**
     * 回滚库存
     * @param id
     * @return
     */
    @Update("update seckill_goods set stock_count = stock_count + 1 where id = #{id}")
    public int rollbackStock(Long id);
}
