package com.atguigu.gmall.list.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.product.SkuInfo;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 搜索微服务监听商品微服务的上下架消息的监听类
 */
@Component
@Log4j2
public class ListListener {

    @Autowired
    private ListService listService;


    /**
     * 上架监听类
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "on_sale_queue")
    public void onSale(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        SkuInfo skuInfo = JSONObject.parseObject(new String(body), SkuInfo.class);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        //数据同步,商品es中进行上架
        listService.addGoods(skuInfo);
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
                    log.error("消费失败的消息内容为:" + skuInfo);
                    log.error("消费失败消息的交换机为:" + messageProperties.getReceivedExchange());
                    log.error("消费失败消息的routingkey为:" + messageProperties.getReceivedRoutingKey());
                }else{
                    //消息没有被消费过,第一次消费失败,再试一下
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                log.error("拒绝失败的异常内容为:" + e1.getMessage());
                log.error("拒绝失败的消息内容为:" + skuInfo);
                log.error("拒绝失败消息的交换机为:" + messageProperties.getReceivedExchange());
                log.error("拒绝失败消息的routingkey为:" + messageProperties.getReceivedRoutingKey());
            }
        }
    }

    /**
     * 上架监听类
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "cancle_sale_queue")
    public void cancleSale(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        long skuId = Long.parseLong(new String(body));
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        //数据同步,商品es中进行上架
        listService.delGoods(skuId);
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
                    log.error("消费失败的消息内容为:" + skuId);
                    log.error("消费失败消息的交换机为:" + messageProperties.getReceivedExchange());
                    log.error("消费失败消息的routingkey为:" + messageProperties.getReceivedRoutingKey());
                }else{
                    //消息没有被消费过,第一次消费失败,再试一下
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                log.error("拒绝失败的异常内容为:" + e1.getMessage());
                log.error("拒绝失败的消息内容为:" + skuId);
                log.error("拒绝失败消息的交换机为:" + messageProperties.getReceivedExchange());
                log.error("拒绝失败消息的routingkey为:" + messageProperties.getReceivedRoutingKey());
            }
        }
    }
}
