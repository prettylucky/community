package com.better.community.config;

import com.better.community.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置我们注册的拦截器
 * @Date 2022/7/9
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    //采用spring security 代替
//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加一个拦截器，默认所有请求都会进行拦截
        //使用excludePathPatterns()进行排除拦截哪些路径
        //此出我们排除所有静态资源请求拦截
        //使用addPathPatterns()指定拦截的请求地址
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg")
                .addPathPatterns("/register", "/login");

        //禁止拦截静态资源
        //拦截所有动态请求，获取根据cookie登录用户信息
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        //拦截未登录用户访问LoginRequired方法
//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        //拦截所有请求均获取未读消息数量（基本所有页面都需要用）
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        //数据统计的拦截器，所有请求都统计
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

    }
}
