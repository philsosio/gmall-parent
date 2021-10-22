package com.atguigu.gmall.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class GmallAuthRequestInterceptor implements RequestInterceptor {

    /**
     * feign发起远程调用前拦截请求,在请求头中添加用户请求时的所有的参数
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //获取主线程的上下文对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes != null){
            HttpServletRequest request = requestAttributes.getRequest();
            //从主线程的request的请求头中获取所有的参数
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()){
                String key = headerNames.nextElement();
                //获取值
                String value = request.getHeader(key);
                //存入当前feign的request请求头中去
                requestTemplate.header(key, value);
            }
        }
    }
}