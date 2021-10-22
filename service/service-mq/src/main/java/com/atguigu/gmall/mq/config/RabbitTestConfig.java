package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 测试mq的配置类,创建队列,交换机,绑定
 */
@Configuration
public class RabbitTestConfig {


    /**
     * 构建队列
     * @return
     */
    @Bean("myQueue")
    public Queue myQueue(){
        return QueueBuilder.durable("nomal_queue").build();
    }

    /**
     * 构建队列
     * @return
     */
    @Bean("myQueue1")
    public Queue myQueue1(){
        return QueueBuilder.durable("dead_queue")
                .withArgument("x-dead-letter-exchange", "dead_exchange")
                .withArgument("x-dead-letter-routing-key", "banzhang.add")
                .build();
    }

    /**
     * 构建交换机
     * @return
     */
    @Bean("myExchange")
    public Exchange myExchange(){
        return ExchangeBuilder.topicExchange("nomal_exchange").build();
    }

    /**
     * 构建交换机
     * @return
     */
    @Bean("myExchange1")
    public Exchange myExchange1(){
        return ExchangeBuilder.topicExchange("dead_exchange").build();
    }

    /**
     * 构建绑定
     * @param myQueue1
     * @param myExchange
     * @return
     */
    @Bean
    public Binding myBinding(@Qualifier("myQueue1") Queue myQueue1,
                             @Qualifier("myExchange") Exchange myExchange){
        return BindingBuilder.bind(myQueue1).to(myExchange).with("user.#").noargs();
    }

    /**
     * 构建绑定
     * @param myQueue
     * @param myExchange1
     * @return
     */
    @Bean
    public Binding myBinding1(@Qualifier("myQueue") Queue myQueue,
                             @Qualifier("myExchange1") Exchange myExchange1){
        return BindingBuilder.bind(myQueue).to(myExchange1).with("banzhang.#").noargs();
    }
}
