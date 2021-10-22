package com.atguigu.gmall.activity.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.pojo.SeckillGoodsNew;
import com.atguigu.gmall.activity.pojo.SeckillOrder;
import com.atguigu.gmall.activity.pojo.UserRecode;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 秒杀异步下单的监听类
 */
@Component
public class SeckillOrderAddListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 秒杀异步下单
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "seckill_queue")
    public void seckillOrderAdd(Channel channel, Message message){
        //获取消息的内容
        String msg = new String(message.getBody());
        //反序列化
        UserRecode userRecode = JSONObject.parseObject(msg, UserRecode.class);
        //获取用户的用户名
        String username = userRecode.getUsername();
        //获取商品的信息
        String goodsId = userRecode.getGoodsId();
        String time = userRecode.getTime();
        SeckillGoodsNew seckillGoodsNew = (SeckillGoodsNew)redisTemplate.boundHashOps("seckillGoods_" + time).get(goodsId);
        //判断商品是否还存在
        if(seckillGoodsNew == null){
            userRecode.setStatus(3);
            userRecode.setMsg("秒杀失败,商品售罄!");
            redisTemplate.boundHashOps("user_record").put(username, userRecode);
            redisTemplate.boundHashOps("user_queue_count").delete(username);
            return;
        }
        //商品是否在活动时间以内time= 2021092210
        if(seckillGoodsNew.getStartTime().getTime() <= System.currentTimeMillis() &&
                System.currentTimeMillis() < seckillGoodsNew.getEndTime().getTime()){
//            //获取商品的库存,判断库存是否足够
//            Integer stockCount = seckillGoodsNew.getStockCount();
//            int stock = stockCount - 1;
            Object o = redisTemplate.boundListOps("seckillGoods_queue_" + goodsId).rightPop();
            if(o == null){
                userRecode.setStatus(3);
                userRecode.setMsg("秒杀失败,商品售罄!");
                redisTemplate.boundHashOps("user_record").put(username, userRecode);
                redisTemplate.boundHashOps("user_queue_count").delete(username);
                return;
            }
            //生成秒杀订单,扣减库存
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(UUID.randomUUID().toString().replace("-", ""));
            seckillOrder.setGoodsId(goodsId);
            seckillOrder.setNum(1);
            seckillOrder.setMoney(seckillGoodsNew.getCostPrice().doubleValue() + "");
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(userRecode.getCreateTime());
            seckillOrder.setStatus("0");
            //将订单的信息存入redis中
            redisTemplate.boundHashOps("seckill_order").put(username, seckillOrder);
            //更新redis中用户的排队信息
            userRecode.setStatus(2);
            userRecode.setMsg("秒杀下单成功,等待支付!");
            userRecode.setMoney(Float.valueOf(seckillOrder.getMoney()));
            userRecode.setOrderId(seckillOrder.getId());
            redisTemplate.boundHashOps("user_record").put(username, userRecode);
            //判断商品是否售罄,若售罄,则删除redis中商品的数据,将商品的数据同步到数据库中去
            Long stock = redisTemplate.boundHashOps("seckillGoods_stock").increment(goodsId + "", -1);
            if(stock == 0){
                //删除redis中的商品的数据
                redisTemplate.boundHashOps("seckillGoods_" + time).delete(goodsId);
                //同步数据库
                seckillGoodsMapper.updateById(seckillGoodsNew);
            }else{
                //更新redis中商品的库存信息
                seckillGoodsNew.setStockCount(stock.intValue());
                redisTemplate.boundHashOps("seckillGoods_" + time).put(goodsId, seckillGoodsNew);
            }
            //发送延迟消息,用于超时未支付订单的处理
            rabbitTemplate.convertAndSend("seckill_nomal_exchange",
                    "seckill.delay",
                    username,
                    new MessagePostProcessor() {
                        /**
                         * 设置消息的超时时间
                         * @param message
                         * @return
                         * @throws AmqpException
                         */
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    //获取消息的属性
                    MessageProperties messageProperties = message.getMessageProperties();
                    //设置过期时间
                    messageProperties.setExpiration("300000");
                    return message;
                }
            });
        }else{
            userRecode.setStatus(3);
            userRecode.setMsg("秒杀失败,商品活动结束!");
            redisTemplate.boundHashOps("user_record").put(username, userRecode);
            redisTemplate.boundHashOps("user_queue_count").delete(username);
            return;
        }
    }
}
