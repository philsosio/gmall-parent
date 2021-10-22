package com.atguigu.gmall.mq.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 测试的消费者
 */
@Component
@Log4j2
public class TestListener {

    /**
     * 消息的消费者
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "nomal_queue")
    public void listener(Channel channel, Message message){
        System.out.println(System.currentTimeMillis());
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息
        byte[] body = message.getBody();
        String msg = new String(body);
        try {
            System.out.println(msg);
            //确认消息
            System.out.println("消息的编号为:" + deliveryTag);
            int i = 1/0;
            /**
             * 1.消息的编号:100
             * 2.是否批量确认消息
             */
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //判断,消息是否被消费过
                if(messageProperties.getRedelivered()){
                    System.out.println("消息已经被消费过一次,并且失败,而且放回队列后重试也失败");
                    channel.basicReject(deliveryTag, false);
                    log.error("消息被消费两次都失败,请排查!!!");
                    log.error("消息的编号为:" + deliveryTag);
                    log.error("消息的内容为:" + msg);
                    log.error("消息的交换机为:" + messageProperties.getReceivedExchange());
                    log.error("消息的routingkey为:" + messageProperties.getReceivedRoutingKey());
                }else{
                    //消息消费出错!!!----拒绝消费---1.丢了 2.放回去
                    /**
                     * 单条拒绝
                     * 1.消息的编号
                     * 2.是否放回队列
                     */
                    System.out.println("消息被拒绝消费,并且被丢弃了!");
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                //打印异常,工作中:写入日志/数据库
                e1.printStackTrace();
            }
        }
    }
}
