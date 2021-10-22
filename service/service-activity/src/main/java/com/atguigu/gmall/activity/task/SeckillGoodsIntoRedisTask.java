package com.atguigu.gmall.activity.task;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.pojo.SeckillGoodsNew;
import com.atguigu.gmall.activity.util.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品从数据库中存入redis中去的定时任务类
 */
@Component
public class SeckillGoodsIntoRedisTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 定时任务
     * cron: , / -的含义
     * fixedRate: 上一次任务执行开始多久后执行下一个任务
     * fixedDelay: 上一次执行结束多久后执行下一个任务
     * initialDelay: 任务第一次什么时候执行
     *    分布式定时任务框架: elastic-job  xxl-job
     *
     */
    @Scheduled(cron = "10/20 * * * * *")
    public void seckillGoodsIntoRedis() throws Exception{
        //获取当前时间所在的时间段,以及后面4个时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            //日期类型进行转换
            //2021-09-22 08:00
            String startTime = DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYY_MM_DDHHMM);
            //2021-09-22 10:00
            String endTime = DateUtil.data2str(DateUtil.addDateHour(dateMenu, 2), DateUtil.PATTERN_YYYY_MM_DDHHMM);
            //将商品的数据写入redis中去
            String key = DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYYMMDDHH);
            //查询每个时间段的商品的数据
            LambdaQueryWrapper<SeckillGoodsNew> wrapper = new LambdaQueryWrapper<>();
            //审核通过的商品
            wrapper.eq(SeckillGoodsNew::getStatus, "1");
            //在活动时间内的商品startTime<=start_time  end_time < endTime
            wrapper.ge(SeckillGoodsNew::getStartTime, startTime);
            wrapper.le(SeckillGoodsNew::getEndTime, endTime);
            //商品的库存必须大于0
            wrapper.gt(SeckillGoodsNew::getStockCount, 0);
            //看商品是不是已经放入redis中去了keys=1,2,3,4,5,6
            Set keys = redisTemplate.boundHashOps("seckillGoods_" + key).keys();
            if(keys != null && keys.size() > 0){
                wrapper.notIn(SeckillGoodsNew::getId, keys);
            }
            //select * from seckill_goods where status = '1' and start_time >= startTime and end_time < endTime
            // and id not in(1,2,3,4,5,6)

            //执行查询
            List<SeckillGoodsNew> seckillGoods = seckillGoodsMapper.selectList(wrapper);
            if(!seckillGoods.isEmpty()){
                for (SeckillGoodsNew seckillGood : seckillGoods) {
                    //获取商品的活动截止时间
                    long goodsEndTime = seckillGood.getEndTime().getTime();
                    //获取当前系统时间
                    long nowTime = System.currentTimeMillis();
                    //计算key的有效时长
                    long liveTime = goodsEndTime - nowTime;
                    //保存商品
                    redisTemplate
                            .boundHashOps("seckillGoods_" + key)
                            .put(seckillGood.getId() + "", seckillGood);
                    //设置商品的过期时间
                    redisTemplate
                            .boundHashOps("seckillGoods_" + key).expire(liveTime, TimeUnit.MILLISECONDS);
                    //构建一个以商品库存数量为长度的元素个数的队列
                    String[] ids = getIds(seckillGood.getStockCount(), seckillGood.getId() + "");
                    redisTemplate.boundListOps("seckillGoods_queue_" + seckillGood.getId()).leftPushAll(ids);
                    //设置队列的过期时间
                    redisTemplate.boundListOps("seckillGoods_queue_" + seckillGood.getId()).expire(liveTime, TimeUnit.MILLISECONDS);
                    //构建一个以商品的库存大小为值的自增数字
                    redisTemplate.boundHashOps("seckillGoods_stock").increment(seckillGood.getId() + "", seckillGood.getStockCount());
                    //设置库存的过期时间
                    redisTemplate.boundHashOps("seckillGoods_stock").expire(liveTime, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    /**
     * 获取指定长度的数组
     * @param num
     * @return
     */
    private String[] getIds(Integer num, String goodsId){
        String[] ids = new String[num];
        for (int i = 0; i < num; i++) {
            ids[i] = goodsId;
        }
        return ids;
    }
}
