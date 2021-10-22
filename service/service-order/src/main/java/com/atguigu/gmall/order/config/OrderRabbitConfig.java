package com.atguigu.gmall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 订单服务的延迟队列交换机的配置文件
 */
@Component
public class OrderRabbitConfig {

    /**
     * 接收带有ttl消息的死信队列
     * @return
     */
    @Bean("delayQueue")
    public Queue delayQueue(){
        return QueueBuilder
                .durable("delay_queue")
                .withArgument("x-dead-letter-exchange","orderDeadExchange")
                .withArgument("x-dead-letter-routing-key","order.timeout")
                .build();
    }

    /**
     * 接收死信的正常队列
     * @return
     */
    @Bean("orderQueue")
    public Queue orderQueue(){
        return QueueBuilder
                .durable("order_queue")
                .build();
    }

    /**
     * 消息成为死信以后,转发死信的交换机
     * @return
     */
    @Bean("orderDeadExchange")
    public Exchange orderDeadExchange(){
        return ExchangeBuilder.directExchange("order_dead_exchange").build();
    }

    /**
     * 转发带有ttl消息的正常交换机
     * @return
     */
    @Bean("orderExchange")
    public Exchange orderExchange(){
        return ExchangeBuilder.directExchange("order_exchange").build();
    }

    /**
     * 带有ttl消息的发送绑定
     * @param orderExchange
     * @param delayQueue
     * @return
     */
    @Bean
    public Binding deadBinding(@Qualifier("orderExchange") Exchange orderExchange,
                               @Qualifier("delayQueue") Queue delayQueue){
        return BindingBuilder.bind(delayQueue).to(orderExchange).with("order.delay").noargs();
    }

    /**
     * 消息成为死信以后出列转发的交换机的绑定
     * @param orderDeadExchange
     * @param orderQueue
     * @return
     */
    @Bean
    public Binding orderBinding(@Qualifier("orderDeadExchange") Exchange orderDeadExchange,
                               @Qualifier("orderQueue") Queue orderQueue){
        return BindingBuilder.bind(orderQueue).to(orderDeadExchange).with("order.timeout").noargs();
    }

}
