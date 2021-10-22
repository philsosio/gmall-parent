package com.atguigu.gmall.activity.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单使用的消息队列的配置
 */
@Component
public class SeckillRabbitConfig {

    /**
     * 创建秒杀队列
     * @return
     */
    @Bean("seckillQueue")
    public Queue seckillQueue(){
        return QueueBuilder.durable("seckill_queue").build();
    }


    /**
     * 创建秒杀交换机
     * @return
     */
    @Bean
    public Exchange seckillExchange(){
        return ExchangeBuilder.directExchange("seckill_exchange").build();
    }


    /**
     * 秒杀交换机和队列绑定
     * @param seckillQueue
     * @param seckillExchange
     * @return
     */
    @Bean
    public Binding seckillBinding(@Qualifier("seckillQueue") Queue seckillQueue,
                                  @Qualifier("seckillExchange") Exchange seckillExchange){
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with("seckill.order").noargs();
    }

    /**
     * 创建秒杀超时队列
     * @return
     */
    @Bean("seckillDelayQueue")
    public Queue seckillDelayQueue(){
        return QueueBuilder
                .durable("seckill_delay_queue")
                .withArgument("x-dead-letter-exchange", "seckill_dead_exchange")
                .withArgument("x-dead-letter-routing-key", "seckill.nomal.delay")
                .build();
    }

    /**
     * 创建秒杀接收死信的正常队列队列
     * @return
     */
    @Bean("seckillNomalQueue")
    public Queue seckillNomalQueue(){
        return QueueBuilder.durable("seckill_nomal_queue").build();
    }

    /**
     * 创建秒杀超时的正常交换机
     * @return
     */
    @Bean("seckillNomalExchange")
    public Exchange seckillNomalExchange(){
        return ExchangeBuilder.directExchange("seckill_nomal_exchange").build();
    }

    /**
     * 创建秒杀超时的正常交换机
     * @return
     */
    @Bean("seckillDeadExchange")
    public Exchange seckillDeadExchange(){
        return ExchangeBuilder.directExchange("seckill_dead_exchange").build();
    }

    /**
     * 秒杀正常交换机和死信队列绑定
     * @param seckillDelayQueue
     * @param seckillNomalExchange
     * @return
     */
    @Bean
    public Binding seckillDeadBinding(@Qualifier("seckillDelayQueue") Queue seckillDelayQueue,
                                  @Qualifier("seckillNomalExchange") Exchange seckillNomalExchange){
        return BindingBuilder.bind(seckillDelayQueue).to(seckillNomalExchange).with("seckill.delay").noargs();
    }


    /**
     * 秒杀死信交换机和正常队列绑定
     * @param seckillNomalQueue
     * @param seckillDeadExchange
     * @return
     */
    @Bean
    public Binding seckillNomalBinding(@Qualifier("seckillNomalQueue") Queue seckillNomalQueue,
                                      @Qualifier("seckillDeadExchange") Exchange seckillDeadExchange){
        return BindingBuilder.bind(seckillNomalQueue).to(seckillDeadExchange).with("seckill.nomal.delay").noargs();
    }



}
