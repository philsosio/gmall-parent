package com.atguigu.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商品管理微服务的启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.atguigu.gmall")
@MapperScan("com.atguigu.gmall.product.mapper")
@EnableFeignClients("com.atguigu.gmall.list.feign")
public class ProductApplication {

    /**
     * @param args:
     */
    public static void main(String[] args) {
        //1.创建了一个IOC容器
        //2.加载启动类的字节码,扫描到了SpringBootApplication这个注解
        //3.将启动类标识为一个配置类
        //4.ComponentScan:包扫描,指定扫描的范围--->启动类所在的包下的所有类的注解以及子包中所有类的注解
        //5.@EnableAutoConfiguration自动装配:加载了可以能使用到的所有的bean对象--->pom文件中的依赖
        SpringApplication.run(ProductApplication.class, args);
    }
}
