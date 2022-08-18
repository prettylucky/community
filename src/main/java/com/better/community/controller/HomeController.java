package com.better.community.controller;

import com.better.community.entity.DiscussPost;
import com.better.community.entity.Page;
import com.better.community.entity.User;
import com.better.community.service.DiscussPostService;
import com.better.community.service.LikeService;
import com.better.community.service.MessageService;
import com.better.community.service.UserService;
import com.better.community.util.CommunityConstant;
import com.better.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
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
public class HomeController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;

    @GetMapping("/")
    public String index() {
        return "forward:/index";
    }

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    //SpringMVC返回model时会自动把page封装进model返回。
    //首次访问首页不会传orderMode此处使用注解给他一个默认值0（即按时间排序）
    public String getIndexPage(HttpServletRequest request, Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){

        page.setRows(discussPostService.findRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.
                findPosts(0, page.getOffset(), page.getLimit(), orderMode);

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for(DiscussPost p : list){
                Map<String, Object> map = new HashMap<>();
                User user1 = userService.findUserById(p.getUserId());
                map.put("user", user1);
                map.put("post", p);
                //查询点赞多少
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, p.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);

        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //System.out.println(authentication.getAuthorities().toString()+"=================================");
//        //查询私信未读消息数（其他所有页面都复用了index.html）
//        int unreadMessageCount = messageService.findUnreadCount(hostHolder.getUser().getId(), null);
//        model.addAttribute("unreadMessageCount", unreadMessageCount);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }

}
