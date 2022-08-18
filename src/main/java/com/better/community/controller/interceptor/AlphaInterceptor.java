package com.better.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器 Interceptor 示例
 * 自定义一个拦截器，并注册到容器中
 * @Date 2022/7/9
 */
@Component
public class AlphaInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    //在Controller之前执行
    //在Controller之前拦截请求进行预处且进行理逻辑判断，决定是否需要拦截该请求
    //如果此方法返回false表示禁止响应此次请求，如果返回true表示运行响应此次请求。（一般返回true）
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //通过打印日志可以看出 handler 对象，是拦截器所拦截到的方法。
        logger.debug("preHandle: " + handler.toString());

        return true;
    }

    //在Controller之后执行（模板引擎渲染之前）
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("postHandle: " + handler);
    }

    //在TemplateEngine渲染之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterCompletion: " + handler);
    }
}
