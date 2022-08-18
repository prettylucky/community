package com.better.community;

import com.better.community.entity.Comment;
import com.better.community.service.CommentService;
import com.better.community.service.FollowService;
import com.better.community.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Map;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/8
 */
@SpringBootTest
public class ServiceTests {
    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;
    @Autowired
    private CommentService commentService;

    @Test
    public void testUser () {
        Map<String, String> map = userService.login("root", "root", 10);
        System.out.println(map);
    }
    @Test
    public void testX() {
        ArrayList<String> strings = new ArrayList<>();
        int[] a = new int[4];
        System.out.println(a[1]);
        String b = "a";
    }

    @Test
    public void sendCode() {
        Map<String, String> map = userService.sendVerifyCode("2426059234@qq.com");
        System.out.println(map);
    }

    @Test
    public void testFollowStatus() {
        System.out.println(followService.findFollowStatus(111, 3, 173));
    }

    @Test
    public void testFindCommentById () {
        Comment commentById = commentService.findCommentById(41);
        System.out.println(commentById.toString());
    }
}
