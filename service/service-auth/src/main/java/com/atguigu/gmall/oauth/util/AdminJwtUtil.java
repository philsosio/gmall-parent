package com.atguigu.gmall.oauth.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class AdminJwtUtil {

    /***
     * 获取管理员令牌
     * @return
     */
    public static String adminJwt(){
        //秘钥->私钥
        Resource resource = new ClassPathResource("atguigu.jks");

        /***
         * 加载证书,读取证书数据
         * 1:证书对象
         * 2:证书的密码
         */
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,"atguigu".toCharArray());

        //把私钥信息当做秘钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("atguigu","atguigu".toCharArray());
        PrivateKey privateKey = keyPair.getPrivate();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
        //并指定加密算法
        RsaSigner rsaSigner = new RsaSigner(rsaPrivateKey);

        //添加载荷数据 payload
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("authorities",new String[]{"admin"});

        //生成令牌 JwtHelper生成令牌/解析令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), rsaSigner);
        return jwt.getEncoded();
    }
}