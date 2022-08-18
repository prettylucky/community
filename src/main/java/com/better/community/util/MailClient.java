package com.better.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 发送邮件功能模块
 * @Date 2022/5/8
 */
@Component
public class MailClient {
    //创建日志对象，用于记录日志
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    // JavaMailSender 邮件类
    @Autowired
    private JavaMailSender mailSender;

    // 通过读取配置文件获取发件人账户名username
    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content) {
        try {
            //获取消息类 mimeMessage，用于封装邮件的相关信息
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            //使用MimeMessageHelper构建消息类
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

            // 构建发件人账户名（通过配置文件获取）
            helper.setFrom(from);
            //构建消息类的接收人
            helper.setTo(to);
            //构建消息类的主题
            helper.setSubject(subject);
            //构建消息类的内容，并可识别HTML内容
            helper.setText(content, true);

            //使用邮件类发送邮件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            // 如果捕获到异常就记录日志（error级别）
            logger.error("邮件发送失败:" + e.getMessage());
            // e.printStackTrace();

            /*
                e.getMessage()：只会获得异常的名称。
                e.printStackTrace()：在控制台打印出详细的异常信息。
             */
        }
    }
}
