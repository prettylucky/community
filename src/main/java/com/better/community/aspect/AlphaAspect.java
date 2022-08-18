package com.better.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 *
 * @Date 2022/7/15
 */
//@Component
//@Aspect     //表示这是一个切面组件
public class AlphaAspect {

    //1.定义切点：使用@Pointcut注解筛选出1个或1批方法（Spring AOP只支持把切面织入到方法中）
    @Pointcut("execution(* com.better.community.service.*.*(..))")
    public void pointcut() {

    }

    //2.定义在切点方法中具体需要织入的逻辑：支持方法前、方法后、方法前和方法后、返回值以后、抛异常时五个地方织入
    //执行顺序
    //    around before
    //    pointcut before
    //    方法执行
    //    pointcut afterReturning
    //    pointcut after
    //    around after
    //切入点前织入
    @Before("pointcut()")
    public void before() {
        System.out.println("pointcut before");
    }
    //切入点后织入
    @After("pointcut()")
    public void after() {
        System.out.println("pointcut after");
    }
    //切入点有返回值之后
    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("pointcut afterReturning");
    }
    //切入点抛异常后
    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("pointcut afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //切入点前
        System.out.println("around before");

        //这句话相当于执行了连接点方法，返回值即连接点方法的返回值
        Object obj = joinPoint.proceed();

        //切入点后
        System.out.println("around after");
        return obj;
    }
}
