package com.better.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Date 7/23/2022
 */

// @EnableScheduling 启用ThreadPoolTaskScheduling的定时功能，不然容器中的该类无法使用。
// @EnableAsync 启用@Async注解，被@Async注解的方法，会默认以多线程(异步)的方式启动
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
