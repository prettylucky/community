package com.better.community;

import com.better.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import javax.mail.MessagingException;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/8
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void setMailClient(){

            mailClient.sendMail("1808993454@qq.com", "TEST", "杨帆你好。");

    }

    @Test
    public void setHTMLMail(){
        //手动使用thymeleaf引擎构建一个HTML页面（最终是一串字符串）
        Context context = new Context();    //Thymeleaf模板引擎上下文对象
        context.setVariable("user", "杨帆");  //往上下文对象中添加一个变量，在模板文件中即可获取
        String html = templateEngine.process("/mail/demo.html", context);


            mailClient.sendMail("1808993454@qq.com", "TEST", html);
    }
}
