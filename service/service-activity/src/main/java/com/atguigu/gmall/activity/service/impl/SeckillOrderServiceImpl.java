package com.atguigu.gmall.activity.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.mapper.SeckillOrderMapper;
import com.atguigu.gmall.activity.pojo.SeckillGoodsNew;
import com.atguigu.gmall.activity.pojo.SeckillOrder;
import com.atguigu.gmall.activity.pojo.UserRecode;
import com.atguigu.gmall.activity.service.SeckillOrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class SeckillOrderServiceImpl implements SeckillOrderService {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 秒杀排队
     *
     * @param time
     * @param id
     * @return
     */
    @Override
    public UserRecode addSeckillOrder(String time, String id) {
        String username = "banzhang";
        //创建排队对象
        UserRecode userRecode = new UserRecode();
        userRecode.setUsername(username);
        userRecode.setTime(time);
        userRecode.setGoodsId(id);
        userRecode.setCreateTime(new Date());

        //重复排队
        Long count = redisTemplate.boundHashOps("user_queue_count").increment(username, 1);
        if(count > 1){
            userRecode.setStatus(3);
            userRecode.setMsg("重复排队!!!!!!");
            return userRecode;
        }
        userRecode.setStatus(1);
        userRecode.setMsg("排队中");
        //将排队信息存入redis中去,让用户查询
        redisTemplate.boundHashOps("user_record").put(username, userRecode);
        //发送消息
        rabbitTemplate.convertAndSend("seckill_exchange", "seckill.order", JSONObject.toJSONString(userRecode));
        //返回排队结果
        return userRecode;
    }

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    /**
     * 秒杀订单的取消
     *
     * @param username
     * @param status   : 1-主动取消 0-被动取消
     */
    @Override
    public void cancleOrder(String username, Short status) {
        //获取订单的信息
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.boundHashOps("seckill_order").get(username);
        //判断订单是否存在
        if(seckillOrder == null){
            return;
        }
        //判断订单的状态是否正确----幂等性
        if(!seckillOrder.getStatus().equals("0")){
            return;
        }
        //修改订单的状态为取消: 主动/被动
        seckillOrder.setStatus(status.equals("1")?"3":"4");
        seckillOrderMapper.insert(seckillOrder);
        redisTemplate.boundHashOps("seckill_order").delete(username);
        //清除标识位
        UserRecode userRecode = (UserRecode)redisTemplate.boundHashOps("user_record").get(username);
        userRecode.setStatus(3);
        userRecode.setMsg(status.equals(1)?"订单取消!":"订单支付超时!");
        redisTemplate.boundHashOps("user_record").put(username, userRecode);
        redisTemplate.boundHashOps("user_queue_count").delete(username);
        //回滚库存!---redis和数据库
        String goodsId = userRecode.getGoodsId();
        String time = userRecode.getTime();
        rollbackSeckillGoodsStock(goodsId, time);
    }

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    /**
     * 回滚秒杀库存
     * @param goodsId
     * @param time
     */
    private void rollbackSeckillGoodsStock(String goodsId, String time) {
        //查询商品是否已经售罄
        SeckillGoodsNew seckillGoodsNew =
                (SeckillGoodsNew)redisTemplate.boundHashOps("seckillGoods_" + time).get(goodsId);
        //如果商品售罄了,回滚数据库的库存
        if(seckillGoodsNew == null){
            //只需要回滚数据库的库存即可,不需要回滚redis的,因为存在定时任务
            seckillGoodsMapper.rollbackStock(Long.parseLong(goodsId));
        }else{
            //其他情况,回滚redis的库存--回滚list队列,增加一个元素
            redisTemplate.boundListOps("seckillGoods_queue_" + goodsId).leftPush(goodsId);
            //回滚库存的展示的自增值
            Long stock = redisTemplate.boundHashOps("seckillGoods_stock").increment(goodsId, 1);
            seckillGoodsNew.setStockCount(stock.intValue());
            //将商品的信息进行覆盖
            redisTemplate.boundHashOps("seckillGoods_" + time).put(goodsId, seckillGoodsNew);
        }
    }

    /**
     * 修改秒杀订单
     *
     * @param map
     * @param status : 1-ali 0-微信
     */
    @Override
    public void updateSeckillOrder(Map<String, String> map, Short status) {
        //获取附加参数
        String attach = null;
        if(status.equals(0)){
            attach = map.get("attach");
        }else{
            attach = map.get("passback_params");
        }
        //反序列化
        Map<String,String> param = JSONObject.parseObject(attach, Map.class);
        //获取用户名
        String username = param.get("username");
        //获取订单的信息
        SeckillOrder seckillOrder =
                (SeckillOrder)redisTemplate.boundHashOps("seckill_order").get(username);
        //判断订单是否存在
        if(seckillOrder == null){
            return;
        }
        //修改订单的支付状态:已支付
        seckillOrder.setStatus("2");
        //将订单的信息存入数据库中去
        seckillOrderMapper.insert(seckillOrder);
        //清除redis中的数据,删除订单数据
        redisTemplate.boundHashOps("seckill_order").delete(username);
        //清除redis中的数据,删除排队标识位
        redisTemplate.boundHashOps("user_record").delete(username);
        redisTemplate.boundHashOps("user_queue_count").delete(username);
    }
}
