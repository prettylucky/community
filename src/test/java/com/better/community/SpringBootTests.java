package com.better.community;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.AfterTestMethod;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

/**
 * @Date 7/24/2022
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    // 保证测试方法的独立性
    // 新建测试数据 --> 测试 --> 销毁测试数据

    @BeforeAll
    public static void beforeClass() {
        System.out.println("before class");
    }

    @AfterAll
    public static void afterClass() {
        System.out.println("after class");
    }

    @BeforeEach
    public void beforeMethod() {
        // 一般用于新建测试数据
        System.out.println("before Method");
    }

    @AfterEach
    public void afterMethod() {
        // 在执行完测试方法后，销毁测试数据
        System.out.println("after Method");
    }


    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }
}
