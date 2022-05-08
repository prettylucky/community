package com.better.community.controller;

import com.better.community.entity.DiscussPost;
import com.better.community.entity.Page;
import com.better.community.entity.User;
import com.better.community.service.DiscussPostService;
import com.better.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/4
 */
@Controller
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    //SpringMVC返回model时会自动把page封装进model返回。
    public String getIndexPage(Model model, Page page){
        page.setRows(discussPostService.findRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findPosts(0, page.getOffset(), page.getLimit());

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for(DiscussPost p : list){
                Map<String, Object> map = new HashMap<>();
                User user = userService.findUserById(Integer.parseInt(p.getUserId()));
                map.put("user", user);
                map.put("post", p);
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);

        return "/index";
    }
}
