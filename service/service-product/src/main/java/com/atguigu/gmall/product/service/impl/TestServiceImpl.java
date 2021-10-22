package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void setValue() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //先获取锁--->setnx-->只有成功的才能进行++的操作,失败的则进行重试
        Boolean aBoolean = redisTemplate.boundValueOps("lock").setIfAbsent(uuid, 2, TimeUnit.SECONDS);
        if(aBoolean){
            //从redis中获取key的值
            Integer i = (Integer)redisTemplate.boundValueOps("java0323").get();
            //若值不为空,则+1
            if(i != null){
                i++;
            }
            //将值写入redis中去
            redisTemplate.boundValueOps("java0323").set(i);
//            //释放锁
//            Object lock = redisTemplate.boundValueOps("lock").get();
//            if(lock.equals(uuid)){
//                //班长准备放锁
//                redisTemplate.delete("lock");
//            }
            //lua表达式---lua脚本释放锁
            DefaultRedisScript<Long> script = new DefaultRedisScript<Long>();
            script.setResultType(Long.class);
            script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
            redisTemplate.execute(script, Arrays.asList("lock"), uuid);
        }else{
            try {
                //休息一秒,再去重试
                Thread.sleep(1000);
                //重试
                setValue();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return;
    }

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void setValueRedission() {
        //获取锁
        RLock lock = redissonClient.getLock("lock");
        try {
            //尝试加锁,成功逻辑操作
            if(lock.tryLock(5000, TimeUnit.SECONDS)){
                //从redis中获取key的值
                Integer i = (Integer)redisTemplate.boundValueOps("java0323").get();
                //若值不为空,则+1
                if(i != null){
                    i++;
                }
                //将值写入redis中去
                redisTemplate.boundValueOps("java0323").set(i);
            }else{
                //失败重试
                setValueRedission();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //释放锁
            lock.unlock();
        }
    }
}
