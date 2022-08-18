package com.better.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 统一记录所有业务组件的日志
 * 日志格式：
 * 用户[192.168.1.1]，在[202-12-22 16:40:00]访问了[com.better.community.service.xxx()].
 * @Date 2022/7/15
 */
@Aspect
@Component
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //切入service包下的所有类的所有方法
    @Pointcut("execution(* com.better.community.service.*.*(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //用户[192.168.1.1]，在[202-12-22 16:40:00]访问了[com.better.community.service.xxx()].
        //通过RequestContextHolder获取request对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        //通过request对象获取用户ip地址
        String ip = request.getRemoteHost();
        //获取当前系统时间并格式化
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //通过JoinPoint获取当前切入方法的类的全包名和方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        //记录日志
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }
}
