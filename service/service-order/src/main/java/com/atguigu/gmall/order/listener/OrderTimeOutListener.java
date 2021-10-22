package com.atguigu.gmall.order.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.OrderConst;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听超时未支付订单消息的监听类
 */
@Component
@Log4j2
public class OrderTimeOutListener {

    @Autowired
    private OrderService orderService;

    /**
     * 监听超时的消息
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "order_queue")
    public void timeOutOrder(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        OrderInfo orderInfo = JSONObject.parseObject(new String(body), OrderInfo.class);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        //修改订单的状态--->取消订单
        orderService.cancleOrder(orderInfo.getId(), OrderConst.TIME_OUT_CANCLE);
        try {
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //判断消息是否已经被消费过了
                if(messageProperties.getRedelivered()){
                    //保存到日志中去,并且拒绝消费消息,丢弃掉
                    channel.basicReject(deliveryTag, false);
                    log.error("消费失败的异常内容为:" + e.getMessage());
                    log.error("消费失败的消息内容为:" + orderInfo);
                    log.error("消费失败消息的交换机为:" + messageProperties.getReceivedExchange());
                    log.error("消费失败消息的routingkey为:" + messageProperties.getReceivedRoutingKey());
                }else{
                    //消息没有被消费过,第一次消费失败,再试一下
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                log.error("拒绝失败的异常内容为:" + e1.getMessage());
                log.error("拒绝失败的消息内容为:" + orderInfo);
                log.error("拒绝失败消息的交换机为:" + messageProperties.getReceivedExchange());
                log.error("拒绝失败消息的routingkey为:" + messageProperties.getReceivedRoutingKey());
            }
        }

    }
}