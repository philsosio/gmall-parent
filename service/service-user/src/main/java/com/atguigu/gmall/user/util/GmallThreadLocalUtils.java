package com.atguigu.gmall.user.util;


/**
 * 本地线程类
 */
public class GmallThreadLocalUtils {

    private final  static ThreadLocal<String> userThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程中的用户
     * @param username
     */
    public static void setUserName(String username){
        userThreadLocal.set(username);
    }

    /**
     * 获取线程中的用户
     * @return
     */
    public static String getUserName( ){
        return userThreadLocal.get();
    }
}