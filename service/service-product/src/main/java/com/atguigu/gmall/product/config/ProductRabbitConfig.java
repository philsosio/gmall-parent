package com.atguigu.gmall.product.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class ProductRabbitConfig {

    /**
     * 下架队列
     * @return
     */
    @Bean("cancleSaleQueue")
    public Queue cancleSaleQueue(){
        return QueueBuilder.durable("cancle_sale_queue").build();
    }

    /**
     * 下架队列
     * @return
     */
    @Bean("onSaleQueue")
    public Queue onSaleQueue(){
        return QueueBuilder.durable("on_sale_queue").build();
    }

    /**
     * 交换机
     * @return
     */
    @Bean("productExchange")
    public Exchange productExchange(){
        return ExchangeBuilder.directExchange("product_exchange").build();
    }

    /**
     * 上架队列绑定
     * @param onSaleQueue
     * @param productExchange
     * @return
     */
    @Bean
    public Binding onSaleBinding(@Qualifier("onSaleQueue") Queue onSaleQueue,
                                 @Qualifier("productExchange") Exchange productExchange){
        return BindingBuilder.bind(onSaleQueue).to(productExchange).with("product.on.sale").noargs();
    }

    /**
     * 下架队列绑定
     * @param cancleSaleQueue
     * @param productExchange
     * @return
     */
    @Bean
    public Binding cancleSaleBinding(@Qualifier("cancleSaleQueue") Queue cancleSaleQueue,
                                 @Qualifier("productExchange") Exchange productExchange){
        return BindingBuilder.bind(cancleSaleQueue).to(productExchange).with("product.cancle.sale").noargs();
    }
}
