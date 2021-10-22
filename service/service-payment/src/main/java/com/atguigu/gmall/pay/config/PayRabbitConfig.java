package com.atguigu.gmall.pay.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 支付相关的rabbit配置
 */
@Component
public class PayRabbitConfig {

    /**
     * 普通订单的支付结果队列
     * @return
     */
    @Bean("normalOrderPayQueueWx")
    public Queue normalOrderPayQueueWx(){
        return QueueBuilder.durable("normal_order_pay_queue_wx").build();
    }
    /**
     * 普通订单的支付结果队列
     * @return
     */
    @Bean("normalOrderPayQueueAli")
    public Queue normalOrderPayQueueAli(){
        return QueueBuilder.durable("normal_order_pay_queue_ali").build();
    }


    /**
     * 交换机
     * @return
     */
    @Bean("normalOrderPayExchange")
    public Exchange productExchange(){
        return ExchangeBuilder.directExchange("normal_order_pay_exchange").build();
    }

    /**
     * 微信支付的消息队列绑定
     * @param normalOrderPayQueueWx
     * @param normalOrderPayExchange
     * @return
     */
    @Bean
    public Binding wxBinding(@Qualifier("normalOrderPayQueueWx") Queue normalOrderPayQueueWx,
                                 @Qualifier("normalOrderPayExchange") Exchange normalOrderPayExchange){
        return BindingBuilder.bind(normalOrderPayQueueWx).to(normalOrderPayExchange).with("normal.order.pay.wx").noargs();
    }

    /**
     * 支付宝支付的消息队列绑定
     * @param normalOrderPayQueueAli
     * @param normalOrderPayExchange
     * @return
     */
    @Bean
    public Binding aliBinding(@Qualifier("normalOrderPayQueueAli") Queue normalOrderPayQueueAli,
                                 @Qualifier("normalOrderPayExchange") Exchange normalOrderPayExchange){
        return BindingBuilder.bind(normalOrderPayQueueAli).to(normalOrderPayExchange).with("normal.order.pay.ali").noargs();
    }
}
