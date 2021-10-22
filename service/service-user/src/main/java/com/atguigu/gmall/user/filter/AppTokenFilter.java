package com.atguigu.gmall.user.filter;

import com.atguigu.gmall.user.util.GmallThreadLocalUtils;
import com.atguigu.gmall.user.util.TokenUtil;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.Map;

/**
 * 过滤器
 */
@Order(1)
@WebFilter(filterName = "appTokenFilter", urlPatterns = "/*")
public class AppTokenFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        //从令牌中获取用户的基本信息
        Map<String, String> userInfo = TokenUtil.getUserInfo();
        //判断是否为空
        if(userInfo != null && userInfo.size() > 0){
            //将用户的名字存入本地线程
            GmallThreadLocalUtils.setUserName(userInfo.get("username"));
        }
        chain.doFilter(req,res);
    }
}