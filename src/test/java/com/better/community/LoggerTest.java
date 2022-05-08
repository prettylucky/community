package com.better.community;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/7
 */
@SpringBootTest
//使用注解引入配置文件/类
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTest {
    private static Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void test() {
        logger.debug("debug msg");
        logger.info("info msg");
        logger.warn("warn msg");
        logger.error("error msg");
    }
}
