package com.better.community.controller;

import com.better.community.annotation.LoginRequired;
import com.better.community.entity.Event;
import com.better.community.entity.Page;
import com.better.community.entity.User;
import com.better.community.event.EventProducer;
import com.better.community.service.CommentService;
import com.better.community.service.FollowService;
import com.better.community.service.UserService;
import com.better.community.util.CommunityConstant;
import com.better.community.util.CommunityUtil;
import com.better.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注相关请求
 * @Date 2022/7/17
 */
@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private CommentService commentService;

    //关注用户，异步请求，返回JSON字符串
    @PostMapping("/follow")
    @LoginRequired
    @ResponseBody
    public String follow(int entityType, int entityId) {
        //用于封装返回数据
        Map<String, Object> map = new HashMap<>();
        //获取当前用户
        User user = hostHolder.getUser();

        //调用service层关注用户
        followService.follow(user.getId(), entityType, entityId);

        //查询用户对实体的关注状态
        boolean followStatus = followService.findFollowStatus(user.getId(), entityType, entityId);
        map.put("followStatus", followStatus);

        //查询实体有多少粉丝
        long followerCount = followService.findEntityFollowerCount(entityType, entityId);
        map.put("followerCount", followerCount);

        //如果实体是用户，则查询该用户关注的用户数量
        if (entityType == ENTITY_TYPE_USER) {
            long followeeCount = followService.findUserFolloweeCount(entityId, ENTITY_TYPE_USER);
            map.put("followeeCount", followeeCount);
        }

        //触发关注事件
        if ((boolean) map.get("followStatus")) {
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0,"操作成功！", map);
    }

    //获取followee页面
    @GetMapping("/followee/{userId}")
    public String getFolloweePage(Page page, @PathVariable int userId, Model model) {
        User user = hostHolder.getUser();
        model.addAttribute("target", userService.findUserById(userId));
        //处理分页信息
        page.setPath("/followee/" + userId);
        page.setLimit(5);
        //查询关注的人一共有多少
        long followeeCount = followService.findUserFolloweeCount(userId, ENTITY_TYPE_USER);
        page.setRows((int) followeeCount);

        //查询所有followees 里面包含了我关注的user 关注时间
        List<Map<String, Object>> followees = followService.findUserFollowees(userId,user.getId(), ENTITY_TYPE_USER, page.getOffset(), page.getLimit());
        model.addAttribute("followees", followees);

        return "/site/followee";
    }

    //获取follower页面
    @GetMapping("/follower/{entityType}/{entityId}")
    public String getFollowerPage(Page page, @PathVariable int entityId, @PathVariable int entityType, Model model) {
        User user = hostHolder.getUser();
        model.addAttribute("target", userService.findUserById(entityId));

        //处理分页信息
        page.setPath("/followee/" + entityType + entityId);
        page.setLimit(5);
        //查询关注的人一共有多少
        long followeeCount = followService.findEntityFollowerCount(entityType, entityId);
        page.setRows((int) followeeCount);
        //查询出所有followers
        List<Map<String, Object>> followers = followService.findEntityFollowers(user.getId(),entityType, entityId, page.getOffset(), page.getLimit());

        model.addAttribute("followers", followers);

        return "/site/follower";
    }
}
