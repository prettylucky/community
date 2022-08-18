package com.better.community.controller.interceptor;

import com.better.community.annotation.LoginRequired;
import com.better.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 拦截未登录状态下请求需要登录的功能
 * @Date 2022/7/10
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //handler是处理当前请求的方法，可以强转为HandlerMethod类。
        //HandlerMethod是对请求方法的封装类，可以获取请求方法相关的信息，此处用来获取请求方法上是否存在 LoginRequired
        //handler instanceof HandlerMethod 表示请求是一个请求方法
        if (handler instanceof HandlerMethod) {
            //把请求方法转换为HandlerMethod类
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //通过HandlerMethod方法获取到方法
            Method method = handlerMethod.getMethod();
            //通过方法获取方法上的LoginRequired注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //当方法上存在loginRequired注解，且用户未登录的情况则拦截该请求，重定向到登录页面
            if (loginRequired != null && hostHolder.getUser() == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
