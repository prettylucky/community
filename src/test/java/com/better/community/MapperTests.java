package com.better.community;

import com.better.community.dao.DiscussPostMapper;
import com.better.community.dao.LoginTicketMapper;
import com.better.community.dao.MessageMapper;
import com.better.community.dao.UserMapper;
import com.better.community.entity.DiscussPost;
import com.better.community.entity.LoginTicket;
import com.better.community.entity.Message;
import com.better.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/4
 */

@SpringBootTest
//使用注解引入配置文件/类
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;


    @Test
    public void DiscussPostMapperTest(){
        List<DiscussPost> discussPosts = discussPostMapper.selectPosts(149, 0, 10, 0);
        for (DiscussPost post : discussPosts){
            System.out.println(post);
        }
        System.out.println(discussPostMapper.selectRows(149));
    }

    @Test
    public void loginTicketTest(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(12);
        loginTicket.setStatus(0);
        loginTicket.setTicket("abc");
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        int n = loginTicketMapper.insertLoginTicket(loginTicket);

        if (n == 1){
            System.out.println("改变了一条记录！");
        }

        LoginTicket loginTicket1 = loginTicketMapper.selectLoginTicket("abc");
        System.out.println(loginTicket1);

        int i = loginTicketMapper.updateLoginTicket("abc", 1);
        if (i == 1)
            System.out.println("更新了一条数据");

        LoginTicket loginTicket2 = loginTicketMapper.selectLoginTicket("abc");
        System.out.println(loginTicket2);

    }

    @Test
    public void testSelectUserByTicket() {
        User user = userMapper.selectByTicket("b36a6a6fdf744b628b9e911427460559");

        System.out.println(user);
    }

    @Test
    public void testMsgMapper() {
        System.out.println(messageMapper.selectConversationCountByUserId(111));
        List<Message> messages = messageMapper.selectLastMessageByUserId(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message.toString());
        }
        System.out.println(messageMapper.selectUnreadCount(111, null));
    }

    @Test
    public void testVim() {
        System.out.println(messageMapper.selectConversationCountByUserId(111));
        List<Message> messages = messageMapper.selectLastMessageByUserId(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message.toString());
        }
        System.out.println(messageMapper.selectUnreadCount(111, null));
        System.out.println("");
    }

}
