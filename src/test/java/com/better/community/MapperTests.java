package com.better.community;

import com.better.community.dao.DiscussPostMapper;
import com.better.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

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

    @Test
    public void DiscussPostMapperTest(){
        List<DiscussPost> discussPosts = discussPostMapper.selectPosts(149, 0, 10);
        for (DiscussPost post : discussPosts){
            System.out.println(post);
        }
        System.out.println(discussPostMapper.selectRows(149));
    }
}
