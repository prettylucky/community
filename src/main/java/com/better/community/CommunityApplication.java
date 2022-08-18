package com.better.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	// @PostConstruct 被修饰的方法会在服务器加载servlet的时候运行，且只会运行一次
	// 一般用于定于一些初始化方法,在构造函数之后执行
	// 该注解由Java提供
	@PostConstruct
	public void	init() {
		// 解决netty启动冲突的问题（es和redis冲突）
		// see Netty4Utils.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
