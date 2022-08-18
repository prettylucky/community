package com.better.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * wkhtmltopdf的命令不会自动创建文件夹
 * 我们这里手动判断一下，如果目标文件夹不存在则创建，如果存在就直接跳过
 * @Date 7/23/2022
 */
@Configuration
public class WkConfig {
    public static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init() {
        //确保wk存放目录存在
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            logger.info("创建WK图片目录：" + wkImageStorage);
        }
    }
}
