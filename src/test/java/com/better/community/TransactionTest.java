package com.better.community;

import com.better.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/13
 */
@SpringBootTest
//引入Spring容器的配置类/配置文件
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTest {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void test1() {
        //声明式事务
        //alphaService.save1();
        //编程式事务
        alphaService.save2();
    }
}
