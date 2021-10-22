package com.atguigu.gmall.gataway.filter;

import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关的全局过滤器
 */
@Component
public class GmallFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 过滤的自定义逻辑
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求的request对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String uri = request.getURI().toString();
        if(uri.contains("product")) {
            return chain.filter(exchange);
        }

        //从url中获取token
        String token = request.getQueryParams().getFirst("token");
        if(StringUtils.isEmpty(token)){
            //从请求头中获取token
            token = request.getHeaders().getFirst("token");
            if(StringUtils.isEmpty(token)){
                //从cookie中获取token
                HttpCookie cookie = request.getCookies().getFirst("token");
                if(cookie != null){
                    String name = cookie.getName();
                    token = cookie.getValue();
                }
            }
        }
        //依然没有token,则返回401
        if(StringUtils.isEmpty(token)){
            response.setStatusCode(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
            return response.setComplete();
        }
        //获取用户当前请求的ip地址
        String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
        //通过ip地址去reids中获取信息
        Object userLoginIp = redisTemplate.boundHashOps("user_login_ip").get(gatwayIpAddress);
        //若获取到的为空,则直接返回401
        if(userLoginIp == null){
            response.setStatusCode(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
            return response.setComplete();
        }
        //判断当前的token和redis中的令牌是否相等,若相等则放行
        if(userLoginIp.equals(token)){
            //将令牌存入请求头中去,并设置名字
            request.mutate().header("Authorization" ,"bearer " + token);
            return chain.filter(exchange);
        }else{
            //否则返回401
            response.setStatusCode(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
            return response.setComplete();
        }
    }

    /**
     * 当前全局过滤器的执行优先级
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
