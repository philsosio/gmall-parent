package com.atguigu.gmall.mq.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * rabbitmq的消息确认配置类
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 修辞一个返回值为void的非静态方法,在本类的构造方法执行完以后执行一次
     */
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this::confirm);
        rabbitTemplate.setReturnCallback(this::returnedMessage);
    }

    /**
     * 消息抵达交换机
     * @param correlationData:元数据
     * @param b: 消息是否抵达交换机
     * @param s: 异常内容是什么
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        if(b){
            log.info("消息抵达交换机成功,内容为:" + JSONObject.toJSONString(correlationData));
        }else{
            log.error("消息抵达交换机失败,原因为:" + s + ",消息的内容为:" +  JSONObject.toJSONString(correlationData));
        }
    }

    /**
     * 消息抵达队列
     * @param message:消息体
     * @param i: 状态码
     * @param s: 错误内容---响应内容
     * @param s1: 交换机名字
     * @param s2: 转发routingkey名字
     */
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        log.error("消息抵达队列失败,内容为:" + new String(message.getBody()));
        log.error("消息抵达队列失败,状态码为:" + i);
        log.error("消息抵达队列失败,响应内容为:" + s);
        log.error("消息抵达队列失败,交换机名字为:" + s1);
        log.error("消息抵达队列失败,转发routingkey名字为:" + s2);
    }
}
