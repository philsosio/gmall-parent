package com.atguigu.gmall.activity.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.activity.pojo.SeckillOrder;
import com.atguigu.gmall.activity.service.SeckillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 超时未支付订单的处理监听类
 */
@Component
@Log4j2
public class SeckillOrderTimeOutListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 秒杀超时订单的处理
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "seckill_nomal_queue")
    public void timeoutOrder(Message message, Channel channel){
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息
        String username = new String(message.getBody());
        //获取订单的信息
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.boundHashOps("seckill_order").get(username);
        try{
            //判断订单是否存在
            if(seckillOrder == null){
                channel.basicAck(deliveryTag, false);
                return;
            }
            //判断订单的状态是否正确----幂等性
            if(!seckillOrder.getStatus().equals("0")){
                channel.basicAck(deliveryTag, false);
                return;
            }
            //取消订单
            seckillOrderService.cancleOrder(username, (short)0);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //消息是否已经被消费过了
                if(messageProperties.isRedelivered()){
                    channel.basicReject(deliveryTag, false);
                    log.error("消息连续两次消费都失败了,订单的内容为:" + JSONObject.toJSONString(seckillOrder));
                    //将处理失败的消息保存到数据库中去
                }else{
                    channel.basicReject(deliveryTag, true);
                    log.error("秒杀订单的消费出错,将进行重试,订单的内容为:"  + JSONObject.toJSONString(seckillOrder));
                }
            }catch (Exception e1){
                log.error("消息消费都失败了,订单的内容为:" + JSONObject.toJSONString(seckillOrder));
                //将处理失败的消息保存到数据库中去
            }
        }
    }
}
