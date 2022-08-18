package com.better.community.controller.advice;

import com.better.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * @ControllerAdvice注解：用于修饰类，表示该类是一个全局配置类
 * 默认ControllerAdvice会扫描所有的bean，一般通过 annotation属性指明只监视 Controller注解的bean
 * 在这个类中，我们可以对Controller进行三种全局配置：
 *    @ExceptionHandler
 *      用于修饰方法，该方法会在Controller出现异常后调用，用于处理捕获到的异常
 *    @ModelAttribute
 *      用于修饰方法，该方法会在Controller方法执行之前被调用，用于提前为Model对象绑定全局参数。
 *    @DataBinder
 *      用于修饰方法，该方法会在Controller方法执行前被调用，用于绑定参数的转换器。
 *
 * 常用于配合 @ExceptionHandler 处理全局异常
 * dao层和service层如果遇到异常最终都会拋到controller层
 * 所有只要在controller层统一拦截异常就行了。
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    //日志类
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    //参数中声明需要捕获的异常，spring会默认把捕获到的异常,request对象,response对象传入
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //1.记录日志
        logger.error("服务器发生异常：" + e.getMessage());
        for ( StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        //2.返回错误信息（普通请求返回一个错误页面，异步请求返回一个JSON提示服务器发生错误）
        //判断请求类型,从请求头的 x-requested-with 字段获取
        String xRequestedWith = request.getHeader("x-requested-with");
        //如果是异步请求
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器发生异常！"));
        } else {
            //如果是普通请求
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
