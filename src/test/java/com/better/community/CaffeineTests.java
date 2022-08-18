package com.better.community;

import com.better.community.service.DiscussPostService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 7/24/2022
 */

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {
    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void testCaffine() {
        System.out.println(discussPostService.findPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findPosts(0, 0, 10, 0));
    }
}
