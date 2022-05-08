package com.better.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * 通过配置类的方式实现向spring容器中托管bean
 * @Date 2022/5/2
 */
//表示当前类是一个Spring配置类
@Configuration
public class AlphaConfig {
    //表示当前方法的返回值类型是一个需要托管的bean，方法名是该bean的id
    @Bean
    public SimpleDateFormat simpleDateFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
