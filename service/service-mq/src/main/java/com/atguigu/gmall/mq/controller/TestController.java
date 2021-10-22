package com.atguigu.gmall.mq.controller;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test/mq")
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 测试发送消息
     * @return
     */
    @GetMapping(value = "/send")
    public String send(){
        rabbitTemplate.convertAndSend("java0323_exchange", "user.add", "新增用户的消息!");
        return "success";
    }

    /**
     * 测试发送消息
     * @return
     */
    @GetMapping(value = "/send1")
    public String send1(){
        System.out.println(System.currentTimeMillis());
        rabbitTemplate.convertAndSend("nomal_exchange", "user.add", "新增用户的消息!", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //获取消息的属性
                MessageProperties messageProperties = message.getMessageProperties();
                //设置消息的过期时间:毫秒
                messageProperties.setExpiration("10000");
                //返回
                return message;
            }
        });
        return "success";
    }
}
